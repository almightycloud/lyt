package org.applicationn.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.transaction.Transactional;

import org.applicationn.domain.ProductExampleAttachment;
import org.applicationn.domain.ProductExampleEntity;

@Named
public class ProductExampleAttachmentService extends BaseService<ProductExampleAttachment> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public ProductExampleAttachmentService(){
        super(ProductExampleAttachment.class);
    }
    
    @Override
    @Transactional
    public long countAllEntries() {
        return entityManager.createQuery("SELECT COUNT(o) FROM ProductExampleAttachment o", Long.class).getSingleResult();
    }

    @Transactional
    public void deleteAttachmentsByProductExample(ProductExampleEntity productExample) {
        entityManager
                .createQuery("DELETE FROM ProductExampleAttachment c WHERE c.productExample = :p")
                .setParameter("p", productExample).executeUpdate();
    }
    
    @Transactional
    public List<ProductExampleAttachment> getAttachmentsList(ProductExampleEntity productExample) {
        if (productExample == null || productExample.getId() == null) {
            return new ArrayList<>();
        }
        // The byte streams are not loaded from database with following line. This would cost too much.
        return entityManager.createQuery("SELECT NEW org.applicationn.domain.ProductExampleAttachment(o.id, o.fileName) FROM ProductExampleAttachment o WHERE o.productExample.id = :id", ProductExampleAttachment.class).setParameter("id", productExample.getId()).getResultList();
    }
}
