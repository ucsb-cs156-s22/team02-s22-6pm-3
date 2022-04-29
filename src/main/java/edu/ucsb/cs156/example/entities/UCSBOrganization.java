package /*main.java.*/edu.ucsb.cs156.example.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor 
@NoArgsConstructor
@Builder
@Entity(name = "ucsborganization")
public class UCSBOrganization {
	@Id
	private String orgCode;
	private String orgTranslationShort;
	private String orgTranslation;
	private boolean inactive;
}
	/*public void setCode(String orgCodeA){orgCode = orgCodeA;}
	public void setTranslationShort(String ots){orgTranslationShort = ots;}
	public void setTranslation(String ot){orgTranslation = ot;}
	public void setInactive(boolean inact){inactive = inact;}*/
/*
  
org.setCode(orgCode);
org.setTranslationShort(orgTranslationShort);
org.setTranslation(orgTranslation);
org.setInactive(inactive);
*/



