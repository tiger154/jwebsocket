App.on('appLoaded', function(aHotLoad){
	// setting the app description
	App.setDescription('Basic Calendar application to get the server-side date/time');
	
	/* 
	 *  FORMATS
	 * 
		Letter	Date or Time Component		Presentation			Examples
		G		Era designator				Text					AD
		y		Year						Year					1996; 96
		M		Month in year				Month					July; Jul; 07
		w		Week in year				Number					27
		W		Week in month				Number					2
		D		Day in year					Number					189
		d		Day in month				Number					10
		F		Day of week in month		Number					2
		E		Day in week					Text					Tuesday; Tue
		a		Am/pm marker				Text					PM
		H		Hour in day (0-23)			Number					0
		k		Hour in day (1-24)			Number					24
		K		Hour in am/pm (0-11)		Number					0
		h		Hour in am/pm (1-12)		Number					12
		m		Minute in hour				Number					30
		s		Second in minute			Number					55
		S		Millisecond					Number					978
		z		Time zone					General time zone		Pacific Standard Time; PST; GMT-08:00
		Z		Time zone					RFC 822 time zone		-0800
	 */
	
	// publishing the 'Calendar' object to the client-side
	App.publish('Calendar', {
		getDate: function(aDateFormat){
			var lFormatter = new Class('java.text.SimpleDateFormat')(aDateFormat);
			var lNow = new Class('java.util.Date')();
			
			return lFormatter.format(lNow);
		}
	});
});