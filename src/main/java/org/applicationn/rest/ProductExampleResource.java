package org.applicationn.rest;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.applicationn.domain.ProductExampleEntity;
import org.applicationn.service.ProductExampleService;

@Path("/productExamples")
@Named
public class ProductExampleResource implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Inject
    private ProductExampleService productExampleService;
    
    /**
     * Get the complete list of ProductExample Entries <br/>
     * HTTP Method: GET <br/>
     * Example URL: /productExamples
     * @return List of ProductExampleEntity (JSON)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProductExampleEntity> getAllProductExamples() {
        return productExampleService.findAllProductExampleEntities();
    }
    
    /**
     * Get the number of ProductExample Entries <br/>
     * HTTP Method: GET <br/>
     * Example URL: /productExamples/count
     * @return Number of ProductExampleEntity
     */
    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    public long getCount() {
        return productExampleService.countAllEntries();
    }
    
    /**
     * Get a ProductExample Entity <br/>
     * HTTP Method: GET <br/>
     * Example URL: /productExamples/3
     * @param id
     * @return A ProductExample Entity (JSON)
     */
    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ProductExampleEntity getProductExampleById(@PathParam("id") Long id) {
        return productExampleService.find(id);
    }
    
    /**
     * Create a ProductExample Entity <br/>
     * HTTP Method: POST <br/>
     * POST Request Body: New ProductExampleEntity (JSON) <br/>
     * Example URL: /productExamples
     * @param productExample
     * @return A ProductExampleEntity (JSON)
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProductExampleEntity addProductExample(ProductExampleEntity productExample) {
        return productExampleService.save(productExample);
    }
    
    /**
     * Update an existing ProductExample Entity <br/>
     * HTTP Method: PUT <br/>
     * PUT Request Body: Updated ProductExampleEntity (JSON) <br/>
     * Example URL: /productExamples
     * @param productExample
     * @return A ProductExampleEntity (JSON)
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProductExampleEntity updateProductExample(ProductExampleEntity productExample) {
        return productExampleService.update(productExample);
    }
    
    /**
     * Delete an existing ProductExample Entity <br/>
     * HTTP Method: DELETE <br/>
     * Example URL: /productExamples/3
     * @param id
     */
    @Path("{id}")
    @DELETE
    public void deleteProductExample(@PathParam("id") Long id) {
        ProductExampleEntity productExample = productExampleService.find(id);
        productExampleService.delete(productExample);
    }
    
}
