$(document).ready(function() {
    
    /* ======= jQuery form validator ======= */ 
    /* Ref: http://jqueryvalidation.org/documentation/ */   
    $(".form").validate({
		messages: {
		
		    firstname: {
    			required: 'Please enter your first name' //You can customise this message
			},
			lastname: {
    			required: 'Please enter your last name' //You can customise this message
			},
			email: {
				required: 'Please enter your email' //You can customise this message
			},	
			number: {
				required: 'Please enter your number' //You can customise this message
			},		
			message: {
				required: 'Please enter your message' //You can customise this message
			}, 			
			reason: {
				required: 'Please select your reason' //You can customise this message
			}, 	
			name: {
    			required: 'Please enter your name' //You can customise this message
			},		
			comment: {
				required: 'Please enter your comment' //You can customise this message
			}
						
		}
		
	});
	
	
});



