package org.applicationn.domain;
import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name="ProductExampleAttachment")
public class ProductExampleAttachment extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    public ProductExampleAttachment() {
        super();
    }
    
    public ProductExampleAttachment(Long id, String fileName) {
        this.setId(id);
        this.fileName = fileName;
    }

    @Size(max = 200)
    private String fileName;
    
    @ManyToOne
    @JoinColumn(name = "PRODUCTEXAMPLE_ID", referencedColumnName = "ID")
    private ProductExampleEntity productExample;

    @Lob
    private byte[] content;

    public byte[] getContent() {
        return this.content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
    
    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ProductExampleEntity getProductExample() {
        return this.productExample;
    }

    public void setProductExample(ProductExampleEntity productExample) {
        this.productExample = productExample;
    }
}
