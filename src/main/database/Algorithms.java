package main.database;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.*;

import lombok.Data;

@Entity(name = "algorithms")
@Data
public class Algorithms implements Serializable {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@Column(name = "uuid", nullable = false)
	private Long uuid;
	
	@Column(name = "name")
	private String name;

	@Column(name = "step_time")
	private Float stepTime;

	@Column(name = "packet_size")
	private Integer packetSize;

	@Column(name = "algorithm_type")
	@Enumerated(EnumType.STRING)
    private AlgorithmType algorithmType;
	
	@Column(name = "target_platform")
    private Integer targetPlatform;

    @Column(name = "public")
    private Integer publicFlag;

    @Column(name = "author")
    private Integer author;

    //private String author;
    
    @Column(name = "test_rig")
    private Integer testRig;

    @Column(name = "bin")
    @Basic(fetch=FetchType.LAZY)
    @Lob
    private byte[] bin;
    
    @Column(name = "last_update")
    private String lastUpdate;

    @Column(name = "model_id")
    private Integer modelId;

    public static enum AlgorithmType {
        Real,
        Simu;
    }
    public void setAuthor(Integer author) {
    	this.author=author;
    }
    public void setName(String name) {
    	this.name=name;
    }
    public void setBin(byte[] bin) {
    	this.bin=bin;
    }
    public void setTestRig(Integer testRig) {
    	this.testRig=testRig;
    } 
    public void setModelId(Integer modelId) {
    	this.modelId=modelId;
    }
    public void setLastUpdate(String lastUpdate) {
    	this.lastUpdate=lastUpdate;
    }
    public void setStepTime(Float stepTime) {
    	this.stepTime=stepTime;
    }
    public void setPacketSize(Integer packetSize) {
    	this.packetSize=packetSize;
    }
    public void setUuid(Long uuid) {
    	this.uuid=uuid;
    }
    public void setPublicFlag(Integer publicFlag) {
    	this.publicFlag=publicFlag;
    }
    public void setTargetPlatform(Integer targetPlatform) {
    	this.targetPlatform=targetPlatform;
    }
}
