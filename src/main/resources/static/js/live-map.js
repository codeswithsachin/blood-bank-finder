// Shared map logic for /hospitals and /find-donors.
// Fixes the earlier complaint: the old map used a fixed center/zoom that
// often left markers scattered off-screen or too zoomed out to be useful.
// This version fits the view to whatever markers actually exist, refreshes
// live data periodically (so stock changes show up without a page reload),
// and offers a "locate me" button using the browser's geolocation API.

var map = L.map('map');
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 18,
    attribution: '&copy; OpenStreetMap contributors'
}).addTo(map);

var liveLayer = L.layerGroup().addTo(map);
var extraMarkers = (typeof window.EXTRA_MAP_MARKERS !== 'undefined') ? window.EXTRA_MAP_MARKERS : [];

function markerColor(type) {
    if (type === 'bloodbank') return '#1a7f4a';
    if (type === 'search') return '#c07a00';
    return '#e0293f';
}

function addMarker(layer, m) {
    var color = markerColor(m.type);
    var icon = L.divIcon({
        className: '',
        html: '<div style="width:14px;height:14px;border-radius:50%;background:' + color +
              ';border:2px solid #fff;box-shadow:0 0 4px rgba(0,0,0,.35);"></div>',
        iconSize: [14, 14]
    });
    return L.marker([m.lat, m.lng], { icon: icon }).addTo(layer).bindPopup('<strong>' + m.title + '</strong><br>' + m.subtitle);
}

function renderLiveMarkers(markers) {
    liveLayer.clearLayers();
    var allPoints = [];

    markers.forEach(function (m) {
        addMarker(liveLayer, m);
        allPoints.push([m.lat, m.lng]);
    });

    extraMarkers.forEach(function (m) {
        addMarker(liveLayer, m).openPopup();
        allPoints.push([m.lat, m.lng]);
    });

    if (allPoints.length > 0) {
        map.fitBounds(allPoints, { padding: [40, 40], maxZoom: 9 });
    } else {
        map.setView([22.9734, 78.6569], 5);
    }
}

function refreshLiveData() {
    fetch('/api/map-data')
        .then(function (res) { return res.json(); })
        .then(function (data) {
            renderLiveMarkers(data);
            var el = document.getElementById('last-updated');
            if (el) el.textContent = 'just now';
        })
        .catch(function () { /* stay on last known data if a refresh fails */ });
}

refreshLiveData();
setInterval(refreshLiveData, 60000);

function locateMe() {
    if (!navigator.geolocation) {
        alert('Your browser does not support location access.');
        return;
    }
    navigator.geolocation.getCurrentPosition(
        function (pos) {
            var lat = pos.coords.latitude, lng = pos.coords.longitude;
            map.setView([lat, lng], 12);
            L.marker([lat, lng]).addTo(map).bindPopup('You are here').openPopup();
        },
        function () {
            alert('Could not get your location. Please check your browser\'s location permission.');
        }
    );
}
