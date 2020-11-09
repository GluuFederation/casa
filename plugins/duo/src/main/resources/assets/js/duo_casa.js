function initialize(host,sigRequest) {
	
	Duo.init({
		'host' : host,
		'sig_request' : sigRequest,
		'post_action' : '/casa/pl/duo-plugin/user/cred_details.zul' 
	});
}

function prepareAlert() {
    alertRef = $('#feedback-duo');
}

function notifyServer() {
	  var widget = zk.$('$readyButton');
	  if(!(zAu && zAu.send)) 
	  { 	
		  alert ("zAu not initialized"); 
		  return; 
	  }
	  zAu.send(new zk.Event(widget, "onData", "success", {toServer:true}));
	  console.log("notified server so that the enrollment can be verified and  persisted");
}