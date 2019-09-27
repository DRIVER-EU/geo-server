package eu.driver.adaptor.geoserver;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.geosolutions.geoserver.rest.GeoServerRESTManager;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import it.geosolutions.geoserver.rest.encoder.coverage.GSCoverageEncoder;
import it.geosolutions.geoserver.rest.manager.GeoServerRESTStoreManager;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.log4j.Logger;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.adapter.core.CISAdapter;
import eu.driver.adapter.properties.ClientProperties;
import eu.driver.api.IAdaptorCallback;
import eu.driver.model.core.DataType;
import eu.driver.model.core.LayerType;
import eu.driver.model.core.MapLayerUpdate;
import eu.driver.model.core.UpdateType;

@RestController
public class GeoServerController implements ResourceProcessor<RepositoryLinksResource>, IAdaptorCallback {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	public String DEFAULT_WS = "driver";
	public String RESTURL;
    public String RESTUSER;
    public String RESTPW;
    public String MapTopic;
    public String RemoteURL;
    
    public java.net.URL URL;

    public GeoServerRESTManager manager;
    public GeoServerRESTReader reader;
    public GeoServerRESTPublisher publisher;
    public GeoServerRESTStoreManager storeManager;
    
	@Override
	public RepositoryLinksResource process(RepositoryLinksResource resource) {
		return resource;
	}
	
	public GeoServerController() {
		
		String defaultLargeFileTopic = ClientProperties.getInstance().getProperty("topicLargeFileUpdate", "large_file_update");
		String defaultRESTURL = ClientProperties.getInstance().getProperty("gsmgr_resturl", "http://localhost:8180/geoserver");
		String defaultRESTUSER = ClientProperties.getInstance().getProperty("gsmgr_restuser", "admin");
		String defaultRESTPW = ClientProperties.getInstance().getProperty("gsmgr_restpw", "geoserver");
		String defaultMapTopic = ClientProperties.getInstance().getProperty("topicMapLayerUpdate", "map_layer_update");
		String defaultRemoteURL = ClientProperties.getInstance().getProperty("remoteURL", "http://localhost:8180/geoserver/driver/wms");
		
		CISAdapter.getInstance().addCallback(this, getenv("topicLargeFileUpdate", defaultLargeFileTopic));
		
		RESTURL = getenv("gsmgr_resturl", defaultRESTURL);
        RESTUSER = getenv("gsmgr_restuser", defaultRESTUSER);
        RESTPW = getenv("gsmgr_restpw", defaultRESTPW);
        MapTopic = getenv("map_update_topic", defaultMapTopic);
        RemoteURL = getenv("remote_url", defaultRemoteURL);

		try {
            URL = new java.net.URL(RESTURL);
            manager = new GeoServerRESTManager(URL, RESTUSER, RESTPW);
            reader = manager.getReader();
            publisher = manager.getPublisher();
            storeManager = manager.getStoreManager();
            
            List<String> wsNames = reader.getWorkspaceNames();
            boolean wsCreated = false;
            for (String wsName : wsNames) {
            	if (wsName.equals(DEFAULT_WS)) {
            		wsCreated = true;
            	}
            }
            if (!wsCreated) {
            	wsCreated = publisher.createWorkspace(DEFAULT_WS);
            }
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
	}
	
	private  String getenv(String envName, String envDefault) {
        String env = System.getenv(envName);
        String prop = System.getProperty(envName, env);
        log.debug("varname " + envName + " --> env:" + env + " prop:"+prop);
        return prop != null ? prop : envDefault;
	}
	
	@Override
	public synchronized void messageReceived(IndexedRecord key, IndexedRecord receivedMessage, String topicName) {
		log.info("-->messageReceived: " + receivedMessage);
		
		if (receivedMessage.getSchema().getName().equalsIgnoreCase("LargeDataUpdate")) {
			eu.driver.model.core.LargeDataUpdate msg = (eu.driver.model.core.LargeDataUpdate) SpecificData
					.get().deepCopy(eu.driver.model.core.LargeDataUpdate.SCHEMA$, receivedMessage);
			
			if (msg.getDataType().equals(DataType.image_geotiff)) {
				//Thats a geoTiff, add this to the GeoServer and expose the Layer
				String url = msg.getUrl().toString();
				String storeName = "";
				String layerName = "";
				try {
					int lastIdx = url.lastIndexOf("/");
					storeName = url.substring(lastIdx+1);
					lastIdx = storeName.lastIndexOf(".");
					layerName = storeName.substring(0, lastIdx);
					
					Path storePath = Paths.get(storeName);
					if (Files.notExists(storePath)) {
						InputStream in = new java.net.URL(url).openStream();
						Files.copy(in, Paths.get(storeName), StandardCopyOption.REPLACE_EXISTING);
					}
						
					File geotiff = new File(storeName);
					boolean pc = publisher.publishExternalGeoTIFF(DEFAULT_WS, storeName, geotiff, layerName,"EPSG:4326",ProjectionPolicy.FORCE_DECLARED,"raster");
					if (!pc) {
						log.error("Error publishing the Layer!");
					} else {
						// send a MapLayerUpdate Message
						MapLayerUpdate mapLayerUpdate = new MapLayerUpdate();
						mapLayerUpdate.setLayerType(LayerType.WMS);
						mapLayerUpdate.setUpdateType(UpdateType.CREATE);
						if (msg.getDescription() != null) {
							mapLayerUpdate.setDescription(msg.getDescription().toString());	
						}
						mapLayerUpdate.setTitle(layerName);
						mapLayerUpdate.setUrl(RemoteURL);
						
						CISAdapter.getInstance().sendMessage(mapLayerUpdate, MapTopic);
						log.info("MapLayerUpdate send:" + mapLayerUpdate.toString());
					}
				} catch (Exception e) {
					log.error("Error creating the Layer on the GeoServer!", e);
				}
			}
		}
		
		log.info("messageReceived-->");
	}
	
	@ApiOperation(value = "removeLayer", nickname = "removeLayer")
	@RequestMapping(value = "/CISGeoServerAdaptor/removeLayer", method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "storeName", value = "the name of the store to be removed", required = true, dataType = "string", paramType = "query") 
	})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> sendTestLayer(
			@RequestParam(value="storeName", required=false) String storeName) {
		log.info("-->removeLayer");
		
		GSCoverageEncoder store = new GSCoverageEncoder();
		/*GSAbstractStoreEncoder
		store.setName(storeName);
		store.setTitle(storeName);
		Boolean removed = storeManager.remove(DEFAULT_WS, store, false);
		if (removed) {
			
		} else {
			log.error("Error removing the store!");
		}*/
		
		
		log.info("removeLayer-->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
}
