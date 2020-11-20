function initialize(host,sigRequest, edit) {
		Duo.init({
			'host' : host,
			'sig_request' : sigRequest,
			'post_action' : '/casa/pl/duo-plugin/user/cred_details.zul' 
		});	
}

function showIframe()
{
	$("#duoDiv").addClass('show'); //collapse('show');
}

function prepareAlert() {
    alertRef = $('#feedback-duo');
}

