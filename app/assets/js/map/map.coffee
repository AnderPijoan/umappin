window.Map or= {}
Map.init = () ->
  L.Icon.Default.imagePath = '/assets/img/leaflet'
  # Create a map in the "map" div, set the view to a given place and zoom
  map = L.map('map').setView [51.505, -0.09], 13

  # Add an OpenStreetMap tile layer
  L.tileLayer(
    'http://{s}.tile.osm.org/{z}/{x}/{y}.png'
    attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
  ).addTo map

  # Initialize the FeatureGroup to store editable layers
  drawnItems = new L.FeatureGroup
  map.addLayer drawnItems

  # Initialize the draw control and pass it the FeatureGroup of editable layers
  drawControl = new L.Control.Draw edit: featureGroup: drawnItems
  map.addControl drawControl

  # Handle draw events
  map.on 'draw:created', (e) ->
    if e.layerType is 'marker'
      console.log "#{e.layer.getLatLng()}"
    drawnItems.addLayer e.layer
    e.layer.bindPopup($('#element-menu-template').html()).openPopup()

  map.on 'draw:edited', () ->
    # Update db to save latest changes.

  map.on 'draw:deleted', () ->
    # Update db to save latest changes.
