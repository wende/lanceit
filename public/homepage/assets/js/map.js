var map;
jQuery(document).ready(function(){

    map = new GMaps({
        div: '#map',
        lat: 51.4995167,
        lng: -0.220892
    });
    map.addMarker({
        lat: 51.4995167,
        lng: -0.220892,
        title: 'Address',      
        infoWindow: {
            content: '<h5 class="subtitle">Neon Tree Solutions HQ</h5><p><span class="region">220 C Blythe Road, London</span><br><span class="postal-code">W14 0HH</span><br><span class="country-name">Anglia</span></p>'
        }
        
    });

});