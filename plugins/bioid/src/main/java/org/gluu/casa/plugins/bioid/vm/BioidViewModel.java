package org.gluu.casa.plugins.bioid.vm;

import java.util.List;

import org.gluu.casa.credential.BasicCredential;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.bioid.BioIDService;
import org.gluu.casa.plugins.bioid.model.BioIDCredential;
import org.gluu.casa.service.ISessionContext;
import org.gluu.casa.ui.UIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.Pair;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;

public class BioidViewModel {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@WireVariable
	private ISessionContext sessionContext;
	private List<BioIDCredential> devices;
	private BioIDCredential newDevice;
	private String accessToken;
	private String apiUrl;
	private String task;

	private static final String trait = BioIDService.TRAIT_FACE_PERIOCULAR;
	private static BioIDService bioIdService;
	
	private String userName;
	private String userId;

	public BioIDCredential getNewDevice() {
		return newDevice;
	}

	public void setNewDevice(BioIDCredential newDevice) {
		this.newDevice = newDevice;
	}

	public List<BioIDCredential> getDevices() {
		return devices;
	}

	/**
	 * Initialization method for this ViewModel.
	 */
	@Init
	public void init() {
		logger.debug("init invoked");
		userName = sessionContext.getLoggedUser().getUserName();
		userId = sessionContext.getLoggedUser().getId();
		bioIdService = BioIDService.getInstance();
		reload();
	}
	
	private void reload() {
		devices = bioIdService.getBioIDDevices(userId);

		try {
			//apiUrl = bioIdService.getScriptPropertyValue("ENDPOINT");

			String bcid = bioIdService.getScriptPropertyValue("STORAGE") + "."
					+ bioIdService.getScriptPropertyValue("PARTITION") + "."
					+ userName.hashCode();
			try {
				if (bioIdService.isEnrolled(bcid, BioIDService.TRAIT_FACE)
						&& bioIdService.isEnrolled(bcid, BioIDService.TRAIT_PERIOCULAR)) {

					accessToken = bioIdService.getAccessToken(bcid, BioIDService.TASK_VERIFY);
					task = BioIDService.TASK_VERIFY;
				} else {
					accessToken = bioIdService.getAccessToken(bcid, BioIDService.TASK_ENROLL);
					task = BioIDService.TASK_ENROLL;
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			// values for task for the UI API are - enrollment , verification,
			// identification and livenessdetection
			/*
			 * Clients.response(new AuInvoke("initPage", accessToken, trait,
			 * BioIDService.TASK_ENROLL.equals(task) ? "enrollment" : "verification",
			 * apiUrl, Executions.getCurrent().getContextPath())); Clients.scrollBy(0, 10);
			 */

		} catch (Exception e) {
			UIUtils.showMessageUI(false);
			logger.error(e.getMessage(), e);
		}
		
	}

	@Command
	public void show(String mode) {
		logger.debug("showBioID");
		try {
			apiUrl = bioIdService.getScriptPropertyValue("ENDPOINT");
			trait = BioIDService.TRAIT_FACE_PERIOCULAR;

			String bcid = bioIdService.getScriptPropertyValue("STORAGE") + "."
					+ bioIdService.getScriptPropertyValue("PARTITION") + "."
					+ userName.hashCode();
			try {
				/*
				 * if (bioIdService.isEnrolled(bcid, BioIDService.TRAIT_FACE) &&
				 * bioIdService.isEnrolled(bcid, BioIDService.TRAIT_PERIOCULAR)) {
				 * accessToken = bioIdService.getAccessToken(bcid,
				 * BioIDService.TASK_VERIFY);
				 * 
				 * task = BioIDService.TASK_VERIFY; } else {
				 */
				accessToken = bioIdService.getAccessToken(bcid, BioIDService.TASK_ENROLL);
				task = BioIDService.TASK_ENROLL;
				/*
				 * }
				 */
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// values for task for the UI API are - enrollment , verification,
			// identification and livenessdetection
			Clients.response(new AuInvoke("initPage", accessToken, trait,
					BioIDService.TASK_ENROLL.equals(task) ? "enrollment" : "verification", apiUrl,
					Executions.getCurrent().getContextPath()));
			Clients.scrollBy(0, 10);

		} catch (Exception e) {
			UIUtils.showMessageUI(false);
			logger.error(e.getMessage(), e);
		}

	}

	Pair<String, String> getDeleteMessages(String nick, String extraMessage) {

		StringBuilder text = new StringBuilder();
		if (extraMessage != null) {
			text.append(extraMessage).append("\n\n");
		}
		text.append(Labels.getLabel("bioid_del_confirm",
				new String[] { nick == null ? Labels.getLabel("general.no_named") : nick }));
		if (extraMessage != null) {
			text.append("\n");
		}

		return new Pair<>(Labels.getLabel("bioid_del_title"), text.toString());

	}

	@Command
	public void delete() {
		logger.debug("delete invoked");
		Pair<String, String> delMessages = getDeleteMessages(Labels.getLabel("face_periocular_traits"), null);
		Messagebox.show(delMessages.getY(), delMessages.getX(), Messagebox.YES | Messagebox.NO,
				true ? Messagebox.EXCLAMATION : Messagebox.QUESTION, event -> {
					if (Messagebox.ON_YES.equals(event.getName())) {
						boolean success = false;
						
						try {
							String bcid = bioIdService.getScriptPropertyValue("STORAGE") + "."
									+ bioIdService.getScriptPropertyValue("PARTITION") + "."
									+ userName.hashCode();
							try {
								success = bis.deleteBioIDCredential(userName);
								if (success) {
									bioIdService.removeFromPersistence(bcid,
											BioIDService.TRAIT_FACE_PERIOCULAR, userId);
								}
							} catch (Exception e) {
								success = false;
								logger.error(e.getMessage(), e);
							}

						} catch (Exception e) {
							// success is false here
							logger.error(e.getMessage(), e);
						}

						updateUI(success, BioidViewModel.this);
					}
				});

	}

	@Listen("onEdit=#editButton")
	public void onEdit(Event event) throws Exception {
		logger.trace(" onEdit invoked");
		enroll();
	}

	private boolean persistEnrollment() throws Exception {
		logger.debug("persistEnrollment onData=#readyButton");
		String bcid = bioIdService.getScriptPropertyValue("STORAGE") + "."
				+ bioIdService.getScriptPropertyValue("PARTITION") + "."
				+ userName.hashCode();
		boolean success = bioIdService.writeToPersistence(bcid, "enroll",
				BioIDService.TRAIT_FACE_PERIOCULAR, userId);
		logger.debug("persistEnrollment onData=#readyButton : " + success);
		return success;
	}

	@Listen("onData=#readyButton")
	public void onData(Event event) throws Exception {
		logger.trace(" onData invoked");
		enroll();
	}
	
	private void enroll() throws Exception {
		updateUI(persistEnrollment(), this);
	}
	
	private updateUI(boolean success, Object bean) {
		UIUtils.showMessageUI(success);
		if (success) {
			reload();
			BindUtils.postNotifyChange(null, null, bean, "devices");
		}
	}

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireEventListeners(view, this);
	}

}
