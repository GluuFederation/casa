package org.gluu.casa.plugins.consent.model;

import org.gluu.casa.plugins.consent.enums.AccountStatus;
import org.gluu.casa.plugins.consent.enums.ConsentStatus;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class DummyConsent extends ConsentResponse{
    public DummyConsent() {
        populateConsent();
    }

    public void populateConsent(){
//        ConsentResponse(consent=[Consent(consentId=lBs6uhzghfs4FZ85zwKXQYG3oRcbkBu_ufVN3_1VQMk=, status=REVOKED, createdDate=2021-11-25T13:01:24Z, updatedDate=2021-11-25T13:03:31Z, expirationDate=null, accounts=[]), Consent(consentId=c725eb5a-6dff-4943-a712-5b960aa39bf8, status=REVOKED, createdDate=2021-09-17T07:53:20Z, updatedDate=2021-11-18T14:44:18Z, expirationDate=null, accounts=[]), Consent(consentId=9ace192c-64bb-42e7-acdc-05a86e1796b0, status=REVOKED, createdDate=2021-09-17T06:48:51Z, updatedDate=2021-11-18T11:33:21Z, expirationDate=null, accounts=[]), Consent(consentId=0c1c7088-0cb4-4f0a-989d-2b3017b4709b, status=REJECTED, createdDate=2021-09-16T11:55:49Z, updatedDate=2021-09-22T14:42:17Z, expirationDate=null, accounts=[]), Consent(consentId=2163323b-db13-47ac-8285-2018b5d30033, status=REJECTED, createdDate=2021-09-17T07:10:35Z, updatedDate=2021-09-21T19:09:11Z, expirationDate=null, accounts=[]), Consent(consentId=36b0a137-07b8-4d6d-86f8-456f5a908f36, status=REVOKED, createdDate=2021-09-20T12:43:43Z, updatedDate=2021-09-21T18:32:40Z, expirationDate=null, accounts=[]), Consent(consentId=BARCLAYS-A-51750868197193, status=REVOKED, createdDate=null, updatedDate=2021-09-21T18:14:27Z, expirationDate=null, accounts=[]), Consent(consentId=7748c4bd-1522-48c2-9c9e-6e1991494b31, status=REVOKED, createdDate=null, updatedDate=2021-09-21T18:11:22Z, expirationDate=null, accounts=[]), Consent(consentId=daf5c3ae-55c8-498a-bd28-7db51e36992f, status=REVOKED, createdDate=2021-09-20T12:46:12Z, updatedDate=2021-09-21T17:45:35Z, expirationDate=null, accounts=[]), Consent(consentId=fa88280b-8b9e-4e8f-9bb1-22da92d3bb3a, status=REVOKED, createdDate=2021-09-20T12:43:32Z, updatedDate=2021-09-21T16:21:13Z, expirationDate=null, accounts=[]), Consent(consentId=16b92318-ae3f-4785-9b65-8dbf07a2b9b0, status=REJECTED, createdDate=2021-09-20T14:19:36Z, updatedDate=2021-09-21T16:02:38Z, expirationDate=null, accounts=[]), Consent(consentId=f8985371-089b-4f2b-857c-56d576f624f1, status=REVOKED, createdDate=2021-09-21T11:11:26Z, updatedDate=2021-09-21T15:44:05Z, expirationDate=null, accounts=[]), Consent(consentId=143870fa-8ccc-49f8-9b7a-8eff68727406, status=AUTHORIZED, createdDate=2021-09-17T07:17:16Z, updatedDate=2021-09-17T07:17:52Z, expirationDate=null, accounts=[Account(accountId=aefea590-0e68-4d8f-b8e3-781a1df49fc0, status=AUTHORIZED), Account(accountId=09a31f10-dbf5-4975-9b93-713657413360, status=AUTHORIZED)]), Consent(consentId=20d37807-03f2-4fa5-ac66-ea9a8d998019, status=AUTHORIZED, createdDate=2021-09-17T07:08:19Z, updatedDate=2021-09-17T07:08:58Z, expirationDate=null, accounts=[Account(accountId=aefea590-0e68-4d8f-b8e3-781a1df49fc0, status=AUTHORIZED), Account(accountId=09a31f10-dbf5-4975-9b93-713657413360, status=AUTHORIZED)]), Consent(consentId=6d57c2c9-600f-4d73-bf33-d99e99dcfc9a, status=AUTHORIZED, createdDate=2021-09-17T07:06:34Z, updatedDate=2021-09-17T07:07:28Z, expirationDate=null, accounts=[Account(accountId=aefea590-0e68-4d8f-b8e3-781a1df49fc0, status=AUTHORIZED), Account(accountId=09a31f10-dbf5-4975-9b93-713657413360, status=AUTHORIZED)]), Consent(consentId=7bb1a29e-c55b-4082-9e7b-b514a52bad27, status=AUTHORIZED, createdDate=2021-09-17T06:55:20Z, updatedDate=2021-09-17T07:05:28Z, expirationDate=null, accounts=[Account(accountId=aefea590-0e68-4d8f-b8e3-781a1df49fc0, status=AUTHORIZED), Account(accountId=09a31f10-dbf5-4975-9b93-713657413360, status=AUTHORIZED)]), Consent(consentId=3c79d1e4-34bd-48c9-9246-130bdef2c7ef, status=REVOKED, createdDate=2021-06-01T09:15:01Z, updatedDate=2021-09-17T00:30:05Z, expirationDate=null, accounts=[]), Consent(consentId=2d932e2f-6f92-42fe-81fd-27a00ddaf340, status=REVOKED, createdDate=2021-06-01T05:55:40Z, updatedDate=2021-09-17T00:30:05Z, expirationDate=null, accounts=[]), Consent(consentId=0aca167d-cb08-4946-801f-db3382beec0a, status=REVOKED, createdDate=2021-06-01T10:53:29Z, updatedDate=2021-09-17T00:30:05Z, expirationDate=null, accounts=[]), Consent(consentId=0a3368e7-dd12-4488-85a4-d1100d711553, status=REVOKED, createdDate=2021-06-01T12:28:06Z, updatedDate=2021-09-17T00:30:05Z, expirationDate=null, accounts=[])])
        List<Consent> consentList = new ArrayList<>();
        setConsentList(consentList);
        Consent consent1 = new Consent();
        consent1.setConsentId("lBs6uhzghfs4FZ85zwKXQYG3oRcbkBu_ufVN3_1VQMk");
        consent1.setProvider("yodlee");
        consent1.setStatus(ConsentStatus.REVOKED);
        consent1.setCreatedDate(OffsetDateTime.now());
        consent1.setUpdatedDate(OffsetDateTime.now());
        consent1.setExpirationDate(OffsetDateTime.now());
        List<Account> accountList1 = new ArrayList<>();
        Account account1 = new Account();
        account1.setAccountId("aefea590-0e68-4d8f-b8e3-781a1df49fc0");
        account1.setStatus(AccountStatus.AUTHORIZED);
        accountList1.add(account1);
        consent1.setAccounts(accountList1);
        getConsentList().add(consent1);
        Consent consent2 = new Consent();
        consent2.setConsentId("c725eb5a-6dff-4943-a712-5b960aa39bf8");
        consent2.setProvider("jans");
        consent2.setStatus(ConsentStatus.REVOKED);
        consent2.setCreatedDate(OffsetDateTime.now());
        consent2.setUpdatedDate(OffsetDateTime.now());
        consent2.setExpirationDate(OffsetDateTime.now());
        Account account2 = new Account();
        account2.setAccountId("09a31f10-dbf5-4975-9b93-713657413360");
        account2.setStatus(AccountStatus.AUTHORIZED);
        List<Account> accountList2 = new ArrayList<>();
        accountList2.add(account2);
        consent2.setAccounts(accountList2);
        getConsentList().add(consent2);
    }
}
