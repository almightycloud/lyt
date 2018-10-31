package org.applicationn.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.PersistenceUnitUtil;
import javax.transaction.Transactional;

import org.applicationn.domain.ProductExampleEntity;
import org.applicationn.service.security.SecurityWrapper;

@Named
public class ProductExampleService extends BaseService<ProductExampleEntity> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public ProductExampleService(){
        super(ProductExampleEntity.class);
    }
    
    @Inject
    private ProductExampleAttachmentService attachmentService;
    
    @Transactional
    public List<ProductExampleEntity> findAllProductExampleEntities() {
        
        return entityManager.createQuery("SELECT o FROM ProductExample o LEFT JOIN FETCH o.image", ProductExampleEntity.class).getResultList();
    }
    
    @Override
    @Transactional
    public long countAllEntries() {
        return entityManager.createQuery("SELECT COUNT(o) FROM ProductExample o", Long.class).getSingleResult();
    }
    
    @Override
    @Transactional
    public ProductExampleEntity save(ProductExampleEntity productExample) {
        String username = SecurityWrapper.getUsername();
        
        productExample.updateAuditInformation(username);
        
        return super.save(productExample);
    }
    
    @Override
    @Transactional
    public ProductExampleEntity update(ProductExampleEntity productExample) {
        String username = SecurityWrapper.getUsername();
        productExample.updateAuditInformation(username);
        return super.update(productExample);
    }
    
    @Override
    protected void handleDependenciesBeforeDelete(ProductExampleEntity productExample) {

        /* This is called before a ProductExample is deleted. Place here all the
           steps to cut dependencies to other entities */
        
        this.attachmentService.deleteAttachmentsByProductExample(productExample);
        
    }

    @Transactional
    public ProductExampleEntity lazilyLoadImageToProductExample(ProductExampleEntity productExample) {
        PersistenceUnitUtil u = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        if (!u.isLoaded(productExample, "image") && productExample.getId() != null) {
            productExample = find(productExample.getId());
            productExample.getImage().getId();
        }
        return productExample;
    }
    
}
