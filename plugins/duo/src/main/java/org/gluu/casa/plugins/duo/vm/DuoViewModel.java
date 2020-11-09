package org.gluu.casa.plugins.duo.vm;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.bioid.BioIDService;
import org.gluu.casa.plugins.bioid.vm.BioidViewModel;
import org.gluu.casa.plugins.duo.DuoService;
import org.gluu.casa.plugins.duo.model.DuoCredential;
import org.gluu.casa.service.ISessionContext;
import org.gluu.casa.service.SndFactorAuthenticationUtils;
import org.gluu.casa.ui.UIUtils;
import org.pf4j.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.Pair;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Messagebox;

import com.duosecurity.duoweb.DuoWeb;
import com.duosecurity.duoweb.DuoWebException;
import com.fasterxml.jackson.core.JsonProcessingException;

public class DuoViewModel {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@WireVariable
	private ISessionContext sessionContext;
	private DuoCredential device;
	private DuoCredential newDevice;
	private SndFactorAuthenticationUtils sndFactorUtils;
	private String host;
	private String sigRequest;
	private String postAction;

	public DuoCredential getNewDevice() {
		return newDevice;
	}

	public void setNewDevice(DuoCredential newDevice) {
		this.newDevice = newDevice;
	}

	public DuoCredential getDevice() {
		return device;
	}

	public void setDevice(DuoCredential device) {
		this.device = device;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getSigRequest() {
		return sigRequest;
	}

	public void setSigRequest(String sigRequest) {
		this.sigRequest = sigRequest;
	}

	public String getPostAction() {
		return postAction;
	}

	public void setPostAction(String postAction) {
		this.postAction = postAction;
	}

	/**
	 * Initialization method for this ViewModel.
	 */
	@Init
	public void init() {
		sndFactorUtils = Utils.managedBean(SndFactorAuthenticationUtils.class);
		sessionContext = Utils.managedBean(ISessionContext.class);
		host = DuoService.getInstance().getScriptPropertyValue("duo_host");

		Session session = Sessions.getCurrent();
		String sigResponse = (String) session.getAttribute("sig_response");
		// after enrollment has been completed

		device = DuoService.getInstance().getDuoCredentials(sessionContext.getLoggedUser().getId());

		if (device == null) {
			// query DUO only if local copy is not present
			String duoUserId = DuoService.getInstance().getUserId(sessionContext.getLoggedUser().getUserName());
			if (duoUserId != null) {
				try {
					// this write will be the local copy of the "duoUserId"
					boolean write = DuoService.getInstance().writeToPersistence(duoUserId,
							sessionContext.getLoggedUser().getId());
					if (write) {
						device = DuoService.getInstance().getDuoCredentials(sessionContext.getLoggedUser().getId());
					}
				} catch (JsonProcessingException e) {
					logger.error("Failed to initialize " + e.getMessage());
				}
			}
		}

		sigRequest = DuoWeb.signRequest(DuoService.getInstance().getScriptPropertyValue("ikey"),
				DuoService.getInstance().getScriptPropertyValue("skey"),
				DuoService.getInstance().getScriptPropertyValue("akey"), sessionContext.getLoggedUser().getUserName());

		logger.debug("init invoked");

	}

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		logger.debug("afterCompose invoked");
		Selectors.wireEventListeners(view, this);
	}

	@Command
	public boolean delete() {

		logger.debug("delete invoked");
		String resetMessages = sndFactorUtils.removalConflict(DuoService.ACR, 1, sessionContext.getLoggedUser()).getY();
		boolean reset = resetMessages != null;
		Pair<String, String> delMessages = getDeleteMessages(resetMessages);
		Messagebox.show(delMessages.getY(), delMessages.getX(), Messagebox.YES | Messagebox.NO,
				true ? Messagebox.EXCLAMATION : Messagebox.QUESTION, event -> {
					if (Messagebox.ON_YES.equals(event.getName())) {

						boolean result = false;
						try {
							result = DuoService.getInstance()
									.deleteDUOCredential(sessionContext.getLoggedUser().getUserName());
							if (result == true) {
								DuoService.getInstance().removeFromPersistence(sessionContext.getLoggedUser().getId());
								BindUtils.postNotifyChange(null, null, DuoViewModel.this, "device");
							}
						} catch (Exception e) {
							logger.error("Error during delete - " + e.getMessage());
						}
						UIUtils.showMessageUI(result);
						Executions.sendRedirect(null);

					}
				});
		return false;

	}

	Pair<String, String> getDeleteMessages(String extraMessage) {

		StringBuilder text = new StringBuilder();
		if (extraMessage != null) {
			text.append(extraMessage).append("\n\n");
		}
		text.append(Labels.getLabel("duo_del_confirm", new String[] { Labels.getLabel("duo_credentials") }));
		if (extraMessage != null) {
			text.append("\n");
		}

		return new Pair<>(Labels.getLabel("duo_del_title"), text.toString());

	}
}
