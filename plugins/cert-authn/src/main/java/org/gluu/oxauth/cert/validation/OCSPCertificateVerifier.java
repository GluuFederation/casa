/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.cert.validation;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.ocsp.OCSPResponse;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.OCSPRespBuilder;
import org.bouncycastle.cert.ocsp.RevokedStatus;
import org.bouncycastle.cert.ocsp.*;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.gluu.oxauth.cert.validation.model.ValidationStatus;
import org.gluu.oxauth.cert.validation.model.ValidationStatus.CertificateValidity;
import org.gluu.oxauth.cert.validation.model.ValidationStatus.ValidatorSourceType;
import org.gluu.oxauth.model.util.SecurityProviderUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Certificate verifier based on OCSP
 * 
 * @author Yuriy Movchan
 * @version March 10, 2016
 */
public class OCSPCertificateVerifier implements CertificateVerifier {

	private static final Logger log = LoggerFactory.getLogger(OCSPCertificateVerifier.class);

	public OCSPCertificateVerifier() {
		SecurityProviderUtility.installBCProvider(true);
	}

	@Override
	public ValidationStatus validate(X509Certificate certificate, List<X509Certificate> issuers, Date validationDate) {
		X509Certificate issuer = issuers.get(0);
		ValidationStatus status = new ValidationStatus(certificate, issuer, validationDate, ValidatorSourceType.OCSP, CertificateValidity.UNKNOWN);

		try {
			
			Principal subjectX500Principal = certificate.getSubjectX500Principal();

			String ocspUrl = getOCSPUrl(certificate);
			log.warn(ocspUrl);
			if (ocspUrl == null) {
				log.error("OCSP URL for '" + subjectX500Principal + "' is empty");
				return status;
			}

			log.error("OCSP URL for '" + subjectX500Principal + "' is '" + ocspUrl + "'");

		
			DigestCalculator digestCalculator = new JcaDigestCalculatorProviderBuilder().build().get(CertificateID.HASH_SHA1);
			log.error("Digest Calculator:" +digestCalculator.toString());
			CertificateID certificateId = new CertificateID(digestCalculator, new JcaX509CertificateHolder(certificate), certificate.getSerialNumber());
			log.error("Certificate ID:" +certificateId.toString());
			// Generate OCSP request
			OCSPReq ocspReq = generateOCSPRequest(certificateId);
			log.error("OCSP Request:" +ocspReq.isSigned());
			// Get OCSP response from server
			//OCSPResp ocspResp = requestOCSPResponse(ocspUrl, ocspReq);
			
			OCSPResp ocspResp =  sendOCSPReq(ocspReq, ocspUrl);
			log.error("oCSP Reponse" +ocspResp.getStatus());
			if (ocspResp.getStatus() != OCSPRespBuilder.SUCCESSFUL) {
				log.error("OCSP response is invalid!");
				status.setValidity(CertificateValidity.INVALID);
				return status;
			}
			SingleResp[] responses;
			boolean foundResponse = false;
			BasicOCSPResp basicResponse = (BasicOCSPResp) ocspResp.getResponseObject();
            responses = (basicResponse == null) ? null : basicResponse.getResponses();
		
	//		SingleResp[] singleResps = basicOCSPResp.getResponses();
			
			if (responses != null && responses.length == 1) {
				SingleResp singleResp = responses[0];
				CertificateID responseCertificateId = singleResp.getCertID();
				
				if (certificateId.equals(responseCertificateId)) {
					foundResponse = true;

					log.error("OCSP validationDate: " + validationDate);
					log.error("OCSP thisUpdate: " + singleResp.getThisUpdate());
					log.error("OCSP nextUpdate: " + singleResp.getNextUpdate());
					log.error("Certificate ID: "+ singleResp.getCertID().getSerialNumber());
					
				
					
					Object certStatus = singleResp.getCertStatus();
					log.error(certStatus.toString());

					
					if (certStatus == CertificateStatus.GOOD) {
						log.error("OCSP status is valid for '" + certificate.getSubjectX500Principal() + "'");
						status.setValidity(CertificateValidity.VALID);
					} else {
						if (singleResp.getCertStatus() instanceof RevokedStatus) {
							log.error("OCSP status is revoked for: " + subjectX500Principal);
							if (validationDate
									.before(((RevokedStatus) singleResp.getCertStatus()).getRevocationTime())) {
								log.error("OCSP revocation time after the validation date, the certificate '"
										+ subjectX500Principal + "' was valid at " + validationDate);
								status.setValidity(CertificateValidity.VALID);
							} else {
								Date revocationDate = ((RevokedStatus) singleResp.getCertStatus()).getRevocationTime();
								log.error("OCSP for certificate '" + subjectX500Principal + "' is revoked since "
										+ revocationDate);
								status.setRevocationDate(revocationDate);
								status.setRevocationObjectIssuingTime(singleResp.getThisUpdate());
								status.setValidity(CertificateValidity.REVOKED);
							}
						}
					}

				}

				
			}

			if (!foundResponse) {
				log.error("There is no matching OCSP response entries");
			}
		} catch (Exception ex) {
			log.error("OCSP exception: ", ex);
		}

		return status;
	}

	 private RevocationStatus getRevocationStatus(SingleResp resp){
	        Object status = resp.getCertStatus();
	        log.error(status.toString());
	        if (status == CertificateStatus.GOOD) {
	            return RevocationStatus.GOOD;
	        } else if (status instanceof RevokedStatus) {
	            return RevocationStatus.REVOKED;
	        } else  {
	            return RevocationStatus.UNKNOWN;
	        }
	       
	    }
	private OCSPReq generateOCSPRequest(CertificateID certificateId) throws OCSPException, OperatorCreationException, CertificateEncodingException {
		OCSPReqBuilder ocspReqGenerator = new OCSPReqBuilder();

		ocspReqGenerator.addRequest(certificateId);

		OCSPReq ocspReq = ocspReqGenerator.build();
		return ocspReq;
	}

	@SuppressWarnings({ "deprecation", "resource" })
	private String getOCSPUrl(X509Certificate certificate) throws IOException {
		ASN1Primitive obj;
		try {
			obj = getExtensionValue(certificate, Extension.authorityInfoAccess.getId());
		} catch (IOException ex) {
			log.error("Failed to get OCSP URL", ex);
			return null;
		}

		if (obj == null) {
			return null;
		}

		AuthorityInformationAccess authorityInformationAccess = AuthorityInformationAccess.getInstance(obj);

		AccessDescription[] accessDescriptions = authorityInformationAccess.getAccessDescriptions();
		for (AccessDescription accessDescription : accessDescriptions) {
			boolean correctAccessMethod = accessDescription.getAccessMethod().equals(X509ObjectIdentifiers.ocspAccessMethod);
			if (!correctAccessMethod) {
				continue;
			}

			GeneralName name = accessDescription.getAccessLocation();
			if (name.getTagNo() != GeneralName.uniformResourceIdentifier) {
				continue;
			}

			DERIA5String derStr = DERIA5String.getInstance((ASN1TaggedObject) name.toASN1Primitive(), false);
			return derStr.getString();
		}

		return null;

	}


	private OCSPResp sendOCSPReq(OCSPReq request, String url) throws IOException {
	    byte[] bytes = request.getEncoded();
	    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
	    connection.setRequestProperty("Content-Type", "application/ocsp-request");
	    connection.setRequestProperty("Accept", "application/ocsp-response");
	    connection.setDoOutput(true);
	   log.error("Sending OCSP request to <{}>", url);
	    DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream()));
	    outputStream.write(bytes);
	    outputStream.flush();
	    outputStream.close();
	    if (connection.getResponseCode() != 200) {
	        log.error("OCSP request has been failed (HTTP {}) - {}", connection.getResponseCode(),
	            connection.getResponseMessage());
	    }
	    try (InputStream in = (InputStream) connection.getContent()) {
	        return new OCSPResp(in);
	    }
	}

	/**
	 * @param certificate
	 *            the certificate from which we need the ExtensionValue
	 * @param oid
	 *            the Object Identifier value for the extension.
	 * @return the extension value as an ASN1Primitive object
	 * @throws IOException
	 */
	private static ASN1Primitive getExtensionValue(X509Certificate certificate, String oid) throws IOException {
		byte[] bytes = certificate.getExtensionValue(oid);
		if (bytes == null) {
			return null;
		}
		ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(bytes));
		ASN1OctetString octs = (ASN1OctetString) aIn.readObject();
		aIn = new ASN1InputStream(new ByteArrayInputStream(octs.getOctets()));
		return aIn.readObject();
	}

	@Override
	public void destroy() {
	}

}
