package com.ncslab.database;

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

    @Column(name="description")
    private String description;

    public static enum AlgorithmType {
        Real,
        Simu;
    }

    public void setDescription(String ipAddress,String monitorPort) {
    	this.description=ipAddress+":"+monitorPort;
    }
}
