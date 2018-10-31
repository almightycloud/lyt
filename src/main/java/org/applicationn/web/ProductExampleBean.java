package org.applicationn.web;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.MimetypesFileTypeMap;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import org.apache.commons.io.IOUtils;
import org.applicationn.domain.ProductExampleAttachment;
import org.applicationn.domain.ProductExampleEntity;
import org.applicationn.domain.ProductExampleImage;
import org.applicationn.service.ProductExampleAttachmentService;
import org.applicationn.service.ProductExampleService;
import org.applicationn.service.security.SecurityWrapper;
import org.applicationn.web.util.MessageFactory;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

@Named("productExampleBean")
@ViewScoped
public class ProductExampleBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(ProductExampleBean.class.getName());
    
    private List<ProductExampleEntity> productExampleList;

    private ProductExampleEntity productExample;
    
    private List<ProductExampleAttachment> productExampleAttachments;
    
    @Inject
    private ProductExampleService productExampleService;
    
    @Inject
    private ProductExampleAttachmentService productExampleAttachmentService;
    
    UploadedFile uploadedImage;
    byte[] uploadedImageContents;
    
    public void prepareNewProductExample() {
        reset();
        this.productExample = new ProductExampleEntity();
        // set any default values now, if you need
        // Example: this.productExample.setAnything("test");
    }

    public String persist() {

        if (productExample.getId() == null && !isPermitted("productExample:create")) {
            return "accessDenied";
        } else if (productExample.getId() != null && !isPermitted(productExample, "productExample:update")) {
            return "accessDenied";
        }
        
        String message;
        
        try {
            
            if (this.uploadedImage != null) {
                try {

                    BufferedImage image;
                    try (InputStream in = new ByteArrayInputStream(uploadedImageContents)) {
                        image = ImageIO.read(in);
                    }
                    image = Scalr.resize(image, Method.BALANCED, 300);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageOutputStream imageOS = ImageIO.createImageOutputStream(baos);
                    Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByMIMEType(
                            uploadedImage.getContentType());
                    ImageWriter imageWriter = (ImageWriter) imageWriters.next();
                    imageWriter.setOutput(imageOS);
                    imageWriter.write(image);
                    
                    baos.close();
                    imageOS.close();
                    
                    logger.log(Level.INFO, "Resized uploaded image from {0} to {1}", new Object[]{uploadedImageContents.length, baos.toByteArray().length});
            
                    ProductExampleImage productExampleImage = new ProductExampleImage();
                    productExampleImage.setContent(baos.toByteArray());
                    productExample.setImage(productExampleImage);
                } catch (Exception e) {
                    FacesMessage facesMessage = MessageFactory.getMessage(
                            "message_upload_exception");
                    FacesContext.getCurrentInstance().addMessage(null, facesMessage);
                    FacesContext.getCurrentInstance().validationFailed();
                    return null;
                }
            }
            
            if (productExample.getId() != null) {
                productExample = productExampleService.update(productExample);
                message = "message_successfully_updated";
            } else {
                productExample = productExampleService.save(productExample);
                message = "message_successfully_created";
            }
        } catch (OptimisticLockException e) {
            logger.log(Level.SEVERE, "Error occured", e);
            message = "message_optimistic_locking_exception";
            // Set validationFailed to keep the dialog open
            FacesContext.getCurrentInstance().validationFailed();
        } catch (PersistenceException e) {
            logger.log(Level.SEVERE, "Error occured", e);
            message = "message_save_exception";
            // Set validationFailed to keep the dialog open
            FacesContext.getCurrentInstance().validationFailed();
        }
        
        productExampleList = null;

        FacesMessage facesMessage = MessageFactory.getMessage(message);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        
        return null;
    }
    
    public String delete() {
        
        if (!isPermitted(productExample, "productExample:delete")) {
            return "accessDenied";
        }
        
        String message;
        
        try {
            productExampleService.delete(productExample);
            message = "message_successfully_deleted";
            reset();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error occured", e);
            message = "message_delete_exception";
            // Set validationFailed to keep the dialog open
            FacesContext.getCurrentInstance().validationFailed();
        }
        FacesContext.getCurrentInstance().addMessage(null, MessageFactory.getMessage(message));
        
        return null;
    }
    
    public void reset() {
        productExample = null;
        productExampleList = null;
        
        productExampleAttachments = null;
        
        uploadedImage = null;
        uploadedImageContents = null;
        
    }

    public void handleImageUpload(FileUploadEvent event) {
        
        Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByMIMEType(
                event.getFile().getContentType());
        if (!imageWriters.hasNext()) {
            FacesMessage facesMessage = MessageFactory.getMessage(
                    "message_image_type_not_supported");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            return;
        }
        
        this.uploadedImage = event.getFile();
        this.uploadedImageContents = event.getFile().getContents();
        
        FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    public byte[] getUploadedImageContents() {
        if (uploadedImageContents != null) {
            return uploadedImageContents;
        } else if (productExample != null && productExample.getImage() != null) {
            productExample = productExampleService.lazilyLoadImageToProductExample(productExample);
            return productExample.getImage().getContent();
        }
        return null;
    }
    
    public List<ProductExampleAttachment> getProductExampleAttachments() {
        if (this.productExampleAttachments == null && this.productExample != null && this.productExample.getId() != null) {
            // The byte streams are not loaded from database with following line. This would cost too much.
            this.productExampleAttachments = this.productExampleAttachmentService.getAttachmentsList(productExample);
        }
        return this.productExampleAttachments;
    }
    
    public void handleAttachmentUpload(FileUploadEvent event) {
        
        ProductExampleAttachment productExampleAttachment = new ProductExampleAttachment();
        
        try {
            // Would be better to use ...getFile().getContents(), but does not work on every environment
            productExampleAttachment.setContent(IOUtils.toByteArray(event.getFile().getInputstream()));
        
            productExampleAttachment.setFileName(event.getFile().getFileName());
            productExampleAttachment.setProductExample(productExample);
            productExampleAttachmentService.save(productExampleAttachment);
            
            // set productExampleAttachment to null, will be refreshed on next demand
            this.productExampleAttachments = null;
            
            FacesMessage facesMessage = MessageFactory.getMessage(
                    "message_successfully_uploaded");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error occured", e);
            FacesMessage facesMessage = MessageFactory.getMessage(
                    "message_upload_exception");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            // Set validationFailed to keep the dialog open
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    public StreamedContent getAttachmentStreamedContent(
            ProductExampleAttachment productExampleAttachment) {
        if (productExampleAttachment != null && productExampleAttachment.getContent() == null) {
            productExampleAttachment = productExampleAttachmentService
                    .find(productExampleAttachment.getId());
        }
        return new DefaultStreamedContent(new ByteArrayInputStream(
                productExampleAttachment.getContent()),
                new MimetypesFileTypeMap().getContentType(productExampleAttachment
                        .getFileName()), productExampleAttachment.getFileName());
    }

    public String deleteAttachment(ProductExampleAttachment attachment) {
        
        productExampleAttachmentService.delete(attachment);
        
        // set productExampleAttachment to null, will be refreshed on next demand
        this.productExampleAttachments = null;
        
        FacesMessage facesMessage = MessageFactory.getMessage(
                "message_successfully_deleted", "Attachment");
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        return null;
    }
    
    public ProductExampleEntity getProductExample() {
        if (this.productExample == null) {
            prepareNewProductExample();
        }
        return this.productExample;
    }
    
    public void setProductExample(ProductExampleEntity productExample) {
        this.productExample = productExample;
    }
    
    public List<ProductExampleEntity> getProductExampleList() {
        if (productExampleList == null) {
            productExampleList = productExampleService.findAllProductExampleEntities();
        }
        return productExampleList;
    }

    public void setProductExampleList(List<ProductExampleEntity> productExampleList) {
        this.productExampleList = productExampleList;
    }
    
    public boolean isPermitted(String permission) {
        return SecurityWrapper.isPermitted(permission);
    }

    public boolean isPermitted(ProductExampleEntity productExample, String permission) {
        
        return SecurityWrapper.isPermitted(permission);
        
    }
    
}
