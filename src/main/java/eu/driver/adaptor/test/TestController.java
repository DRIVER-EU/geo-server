package eu.driver.adaptor.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.log4j.Logger;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.adapter.core.CISAdapter;
import eu.driver.adapter.excpetion.CommunicationException;
import eu.driver.api.IAdaptorCallback;
import eu.driver.model.core.DataType;
import eu.driver.model.core.LargeDataUpdate;
import eu.driver.model.core.LayerType;
import eu.driver.model.core.MapLayerUpdate;
import eu.driver.model.core.UpdateType;

@RestController
public class TestController implements ResourceProcessor<RepositoryLinksResource>, IAdaptorCallback {

	private Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public RepositoryLinksResource process(RepositoryLinksResource resource) {
		return resource;
	}
	
	public TestController() {
		CISAdapter.getInstance().addCallback(this, getenv("topicMapLayerUpdate","standard_map_layer_update"));
	}
	
	@Override
	public void messageReceived(IndexedRecord key, IndexedRecord receivedMessage, String topicName) {
		log.info("-->messageReceived: " + receivedMessage.getSchema().getName());
		//eu.driver.model.edxl.EDXLDistribution msgKey = (eu.driver.model.edxl.EDXLDistribution) SpecificData.get().deepCopy(eu.driver.model.edxl.EDXLDistribution.SCHEMA$, key);
	}
	
	@ApiOperation(value = "sendTestLayer", nickname = "sendTestLayer")
	@RequestMapping(value = "/CISGeoServerAdaptor/sendTestLayer", method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "layerURL", value = "the URL where the layer can retrieved", required = true, dataType = "string", paramType = "query"),
		@ApiImplicitParam(name = "topicName", value = "the name of the topic to which the messag needs to be send", required = true, dataType = "string", paramType = "query"),
		@ApiImplicitParam(name = "description", value = "the name of the topic to which the messag needs to be send", required = true, dataType = "string", paramType = "query")})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> sendTestLayer(
			@RequestParam(value="layerURL", required=false) String layerURL,
			@RequestParam(value="topicName", required=false) String topicName,
			@RequestParam(value="description", required=false) String description) {
		log.info("-->sendTestLayer");
		
		LargeDataUpdate msg = new LargeDataUpdate();
		msg.setDataType(DataType.image_geotiff);
		msg.setTitle("TestLayer");
		msg.setUrl(layerURL);
		msg.setDescription(description);
		
		try {
			CISAdapter.getInstance().sendMessage(msg, topicName);
		} catch (CommunicationException e) {
			return new ResponseEntity<Boolean>(false, HttpStatus.OK);
		}
		
		log.info("sendTestLayer-->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendMapLayer", nickname = "sendMapLayer")
	@RequestMapping(value = "/CISGeoServerAdaptor/sendMapLayer", method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "layerURL", value = "the URL where the layer can retrieved", required = true, dataType = "string", paramType = "query"),
		@ApiImplicitParam(name = "layerName", value = "the Name of the layer", required = true, dataType = "string", paramType = "query"),
		@ApiImplicitParam(name = "topicName", value = "the name of the topic to which the messag needs to be send", required = true, dataType = "string", paramType = "query") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> sendMapLayer(
			@RequestParam(value="layerURL", required=false) String layerURL,
			@RequestParam(value="layerName", required=false) String layerName,
			@RequestParam(value="topicName", required=false) String topicName) {
		log.info("-->sendMapLayer");
		
		MapLayerUpdate mapLayerUpdate = new MapLayerUpdate();
		mapLayerUpdate.setLayerType(LayerType.WMS);
		mapLayerUpdate.setUrl(layerURL);
		mapLayerUpdate.setTitle(layerName);
		mapLayerUpdate.setUpdateType(UpdateType.UPDATE);
		
		try {
			CISAdapter.getInstance().sendMessage(mapLayerUpdate, topicName);
		} catch (CommunicationException e) {
			return new ResponseEntity<Boolean>(false, HttpStatus.OK);
		}
		
		log.info("sendMapLayer-->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@ApiOperation(value = "downloadGeoTiFF", nickname = "downloadGeoTiFF")
	@RequestMapping(value = "/CISGeoServerAdaptor/downloadGeoTiFF/{filename:.+}", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = byte[].class),
			@ApiResponse(code = 400, message = "Bad Request", response = byte[].class),
			@ApiResponse(code = 500, message = "Failure", response = byte[].class) })
	public ResponseEntity<byte[]> downloadGeoTiFF(@PathVariable(value="filename", required=false) String fileName) {
		log.info("-->downloadGeoTiFF");
		File file = new File("files/" + fileName);
		byte[] fileContent = null;
		try {
			fileContent = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			log.error("Error loading the file!", e);
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("image/tiff"));
	    headers.setCacheControl(CacheControl.noCache().getHeaderValue());
	    headers.setContentDispositionFormData("attachment", fileName); 
	    log.info("downloadGeoTiFF-->");
	    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
	}
	
	private  String getenv(String envName, String envDefault) {
        String env = System.getenv(envName);
        String prop = System.getProperty(envName, env);
        log.debug("varname " + envName + " --> env:" + env + " prop:"+prop);
        return prop != null ? prop : envDefault;
	}
	
	
}
