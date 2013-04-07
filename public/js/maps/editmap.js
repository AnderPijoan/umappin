/** ---------------------------------------------------------------------------------- **/
/** ----------------------------- BASE LAYERS ------------------------------- **/
/** ---------------------------------------------------------------------------------- **/

/* --------------------- Base Layers  (currently Bing or OSM) --------------------*/
function getBaseLayersFromSource(source) {
     if (source == "bing")
        return layersFromBing();
     else if (source == "osm")
        return layersFromOSM();
    return null;
}

/* --------------------- Bing Layers --------------------*/
function layersFromBing() {
    var apiKey = "AqTGBsziZHIJYYxgivLBf0hVdrAk9mWO5cQcb8Yux8sW5M8c8opEC2lZqKR1ZZXf";
    var layers = [
       new OpenLayers.Layer.Bing({
            key: apiKey,
            type: "Road",
            metadataParams: {mapVersion: "v1"},
            isBaseLayer: true
        }),
        new OpenLayers.Layer.Bing({
            key: apiKey,
            type: "Aerial",
            isBaseLayer: true
        }),
        new OpenLayers.Layer.Bing({
            key: apiKey,
            type: "AerialWithLabels",
            name: "Bing Aerial With Labels",
            isBaseLayer: true
        })
    ];
    return layers;
}

/* --------------------- OSM Layers --------------------*/
function layersFromOSM() {
    var layers = [
           new OpenLayers.Layer.OSM(
                "OpenCycleMap",
                [
                    "http://a.tile.opencyclemap.org/cycle/${z}/${x}/${y}.png",
                    "http://b.tile.opencyclemap.org/cycle/${z}/${x}/${y}.png",
                    "http://c.tile.opencyclemap.org/cycle/${z}/${x}/${y}.png"
                ],
                {
                    layers: "basic",
                    isBaseLayer: true,
                    resolutions: [156543.03390625, 78271.516953125, 39135.7584765625,
                                  19567.87923828125, 9783.939619140625, 4891.9698095703125,
                                  2445.9849047851562, 1222.9924523925781, 611.4962261962891,
                                  305.74811309814453, 152.87405654907226, 76.43702827453613,
                                  38.218514137268066, 19.109257068634033, 9.554628534317017,
                                  4.777314267158508, 2.388657133579254, 1.194328566789627,
                                  0.5971642833948135, 0.25, 0.1, 0.05],
                    serverResolutions: [156543.03390625, 78271.516953125, 39135.7584765625,
                                        19567.87923828125, 9783.939619140625,
                                        4891.9698095703125, 2445.9849047851562,
                                        1222.9924523925781, 611.4962261962891,
                                        305.74811309814453, 152.87405654907226,
                                        76.43702827453613, 38.218514137268066,
                                        19.109257068634033, 9.554628534317017,
                                        4.777314267158508, 2.388657133579254,
                                        1.194328566789627, 0.5971642833948135],
                    transitionEffect: 'resize'
                }
            )
    ];
    return layers;
}

/** ---------------------------------------------------------------------------------- **/
/** --------------------------- OLMap Class ---------------------------------- **/
/** ---------------------------------------------------------------------------------- **/
var OLMap = function(mapContainer, source) {
    var container = mapContainer || 'map';
    OpenLayers.Map.call (
        this,
        container,
        {
            theme: null,
            projection: "EPSG:900913",
            fractionalZoom: true
         }
     );
    this.src = source || 'osm';

    this.featuresIndex = [];
    this.controlList = {};
    this.currPopup = null;
    
    this.reloadBaseMap();
};
OLMap.prototype  = Object.create(OpenLayers.Map.prototype);
OLMap.prototype.reloadBaseMap = function() {  
     var layersToRemove = [];
     for (var key in this.layers)
        if (this.layers[key].isBaseLayer)
            layersToRemove.push(this.layers[key]);
     for (var key in layersToRemove)
            this.removeLayer(layersToRemove[key], false);
     this.addLayers(getBaseLayersFromSource(this.src));
};
OLMap.prototype.changeSource = function(source) {
     this.src = source;
     this.reloadBaseMap();
};




/** ---------------------------------------------------------------------------------- **/
/** ------------------------- Map inittialization ------------------------------- **/
/** ---------------------------------------------------------------------------------- **/
function initMap(map){

    /** ------------------------- Drawing layer setup------------------------------- **/
    OpenLayers.Feature.Vector.style['default']['strokeWidth'] = '2';
    // allow testing of specific renderers via "?renderer=Canvas", etc
    var renderer = OpenLayers.Util.getParameters(window.location.href).renderer;
    renderer = (renderer) ? [renderer] : OpenLayers.Layer.Vector.prototype.renderers;
    vectors = new OpenLayers.Layer.Vector("Vectors", {
        renderers: renderer
    });
    map.addLayer(vectors);
    
    /** ------------------------- Events & Debugging stuff ------------------------------- **/
    if (console && console.log) {
        function report(event) {
            console.log(event.type, event.feature ? event.feature.id : event.components);
        }
        vectors.events.on({
            "beforefeaturemodified": report,
            "featuremodified": report,
            "afterfeaturemodified": function(evt) {
                report(evt);
                modifyLocalFeature(evt.feature, map);
            },
            "vertexmodified": report,
            "sketchmodified": report,
            "sketchstarted": report,
            "sketchcomplete": function(evt) {
                report(evt);
                addLocalFeature(evt.feature, map);
            }
        });
    }

    /** --------------------------- Control List ---------------------------------- **/
    map.controlList = {
        point: new OpenLayers.Control.DrawFeature(
            vectors,
            OpenLayers.Handler.Point,
            { 'displayClass': 'olControlDrawFeaturePoint' }
        ),
        line: new OpenLayers.Control.DrawFeature(
            vectors,
            OpenLayers.Handler.Path,
            { 'displayClass': 'olControlDrawFeaturePath' }
        ),
        polygon: new OpenLayers.Control.DrawFeature(
            vectors,
            OpenLayers.Handler.Polygon,
            { 'displayClass': 'olControlDrawFeaturePolygon' }
        ),
        regular: new OpenLayers.Control.DrawFeature(
            vectors,
            OpenLayers.Handler.RegularPolygon,
            { 
                'displayClass': 'olControlDrawFeatureRegularPolygon',
                handlerOptions: { sides: 8 } 
            }
        ),
        modify: new OpenLayers.Control.ModifyFeature(
            vectors,
            {
                createVertices: true,
                mode: OpenLayers.Control.ModifyFeature.RESHAPE
            }
        ),
        drag: new OpenLayers.Control.DragFeature(
            vectors,
            {
                onComplete: function(feature, pixel) {
                    modifyLocalFeature(feature, map);
                }
            }
        ),
        save: new OpenLayers.Control.Button(
            {
                displayClass: "olControlSaveSessionToLocalButton",
                trigger: saveSessionToLocal
        })
    };

    /** --------------------------- Toolbar ---------------------------------- **/
    var toolBarControls =  [];
    for(var key in map.controlList) {
        //map.addControl(map.controlList[key]);
        toolBarControls.push(map.controlList[key]);
    }

    var toolbar = new OpenLayers.Control.Panel({
       displayClass: 'olControlEditingToolbar',
       defaultControl: toolBarControls[0]
    });
    toolbar.addControls(toolBarControls);

    /** --------------------------- Cache controls ---------------------------------- **/
    // try cache before loading from remote resource
    map.controlList.cacheRead1 = new OpenLayers.Control.CacheRead({
        eventListeners: {
            activate: function() {
                map.controlList.cacheRead2.deactivate();
            }
        }
    });
    // try loading from remote resource and fall back to cache
    map.controlList.cacheRead2 = new OpenLayers.Control.CacheRead({
        autoActivate: false,
        fetchEvent: "tileerror",
        eventListeners: {
            activate: function() {
                map.controlList.cacheRead1.deactivate();
            }
        }
    });
    map.controlList.cacheWrite = new OpenLayers.Control.CacheWrite({
        imageFormat: "image/jpeg",
        eventListeners: {
            cachefull: function() {
                if (seeding) {
                    stopSeeding();
                }
                alert("Cache full.");
            }
        }
    });

    map.addControls( [ map.controlList.cacheRead1, map.controlList.cacheRead2, map.controlList.cacheWrite ] );

    /** --------------------------- Other Controls ---------------------------------- **/
    map.addControl(new OpenLayers.Control.LayerSwitcher());
    map.addControl(new OpenLayers.Control.MousePosition());
    map.addControl(new OpenLayers.Control.PanZoomBar());

    /** --------------------------- Add stuff to map ---------------------------------- **/
    map.addControl(toolbar);
    map.setCenter(new OpenLayers.LonLat(0, 0), 3);
}
 /*
function update() {
    // reset modification mode
    controls.modify.mode = OpenLayers.Control.ModifyFeature.RESHAPE;
    var rotate = document.getElementById("rotate").checked;
    if(rotate) {
        controls.modify.mode |= OpenLayers.Control.ModifyFeature.ROTATE;
    }
    var resize = document.getElementById("resize").checked;
    if(resize) {
        controls.modify.mode |= OpenLayers.Control.ModifyFeature.RESIZE;
        var keepAspectRatio = document.getElementById("keepAspectRatio").checked;
        if (keepAspectRatio) {
            controls.modify.mode &= ~OpenLayers.Control.ModifyFeature.RESHAPE;
        }
    }
    var drag = document.getElementById("drag").checked;
    if(drag) {
        controls.modify.mode |= OpenLayers.Control.ModifyFeature.DRAG;
    }
    if (rotate || drag) {
        controls.modify.mode &= ~OpenLayers.Control.ModifyFeature.RESHAPE;
    }
    controls.modify.createVertices = document.getElementById("createVertices").checked;
    var sides = parseInt(document.getElementById("sides").value);
    sides = Math.max(3, isNaN(sides) ? 0 : sides);
    controls.regular.handler.sides = sides;
    var irregular =  document.getElementById("irregular").checked;
    controls.regular.handler.irregular = irregular;
}

function toggleControl(element) {
    for(key in controls) {
        var control = controls[key];
        if(element.value == key && element.checked) {
            control.activate();
        } else {
            control.deactivate();
        }
    }
}
*/

/** ----------------------------------------------------------------------------------- **/
/** --------------------------------- STORAGE -------------------------------- **/
/** ---------------------------------------------------------------------------------- **/

/** -------------------------- Adding a Feature to Local Storage (JSON) ------------------------- **/
function addLocalFeature(feature, map, persistent) {
    persistent = persistent || false;
    var data = persistent   ? window.localStorage.getItem('featuresIndex') 
                            : window.sessionStorage.getItem('featuresIndex')
    var featuresIndex = JSON.parse(data) || []; // Improve this, load into a global on startup

    featuresIndex.push(feature.id);

    var json = new OpenLayers.Format.GeoJSON(
        {
            'internalProjection': map.baseLayer.projection,
            'externalProjection': new OpenLayers.Projection("EPSG:4326")
        }
    );
    // second argument for pretty printing (geojson only)
    var str = json.write(feature, false);
    if (persistent) {
        window.localStorage.setItem(feature.id, str);
        window.localStorage.setItem('featuresIndex', JSON.stringify(featuresIndex));
    } else {
        window.sessionStorage.setItem(feature.id, str);
        window.sessionStorage.setItem('featuresIndex', JSON.stringify(featuresIndex));
    }
    
}

/** -------------------------- Updting a Feature to Local Storage (JSON) ------------------------- **/
function modifyLocalFeature(feature, map, persistent) {
    persistent = persistent || false;
    console.log(feature);

    var json = new OpenLayers.Format.GeoJSON(
        {
            'internalProjection': map.baseLayer.projection,
            'externalProjection': new OpenLayers.Projection("EPSG:4326")
        }
    );
    // second argument for pretty printing (geojson only)
    var str = json.write(feature, false);
    if (persistent) {
        window.localStorage.setItem(feature.id, str);
    } else {
        window.sessionStorage.setItem(feature.id, str);
    }

}

/** -------------------------- Load features from Local Storage (JSON) ------------------------- **/
function loadLocalFeatures(map, persistent) {
    persistent = persistent || false;
    var data = persistent   ? window.localStorage.getItem('featuresIndex') 
                            : window.sessionStorage.getItem('featuresIndex')
    var featuresIndex = JSON.parse(data) || [];

    if (featuresIndex.length == 0) {
        if (!persistent)
            return loadLocalFeatures(map, true);
        else
            return;
    }

    var json = new OpenLayers.Format.GeoJSON(
        {
            'internalProjection': map.baseLayer.projection,
            'externalProjection': new OpenLayers.Projection("EPSG:4326")
        }
    );
    map.featuresIndex = [];
    var featuresLayers =map.getLayersByName("Vectors");
    for (var key in featuresIndex) {
        var feature = json.read( persistent ? window.localStorage.getItem(featuresIndex[key]) 
                                            : window.sessionStorage.getItem(featuresIndex[key]));

        console.log(feature);
        console.log(feature[0].id);


        map.featuresIndex[feature[0].id] = feature;
        featuresLayers[0].addFeatures(feature[0]);

        var str = json.write(feature[0], false);
        if (persistent) {
            window.localStorage.removeItem(featuresIndex[key]);
            window.localStorage.setItem(feature[0].id, str);
        }
        window.sessionStorage.removeItem(featuresIndex[key]);
        window.sessionStorage.setItem(feature[0].id, str);
        featuresIndex[key] =  feature[0].id;

    }

    if (persistent) {
        window.localStorage.setItem('featuresIndex', JSON.stringify(featuresIndex));
    }
    window.sessionStorage.setItem('featuresIndex', JSON.stringify(featuresIndex));

}

/** -------------------------- Save session to Local Storage (JSON) ------------------------- **/
function saveSessionToLocal() {
    var data = window.sessionStorage.getItem('featuresIndex')
    var featuresIndex = JSON.parse(data) || [];
    for (var key in featuresIndex)
        window.localStorage.setItem(featuresIndex[key], window.sessionStorage.getItem(featuresIndex[key]));
    window.localStorage.setItem('featuresIndex', data);
}

/** ----------------------------------------------------------------------------------- **/
/** ----------------------------------- MAIN -------------------------------------- **/
/** ---------------------------------------------------------------------------------- **/
window.Map = window.Map || {}
Map.init = function() {
    /** ----------------- Map declaration / initialization ------------------ **/
    OpenLayers.ImgPath = "/assets/img/ol/";
    var olMap = new OLMap();
    initMap(olMap);
    loadLocalFeatures(olMap);
    /** ----------------------------------- Externals -------------------------------------- **/
    $('#mapSource').change(function() {
        olMap.changeSource($('#mapSource').val());
    });

}
