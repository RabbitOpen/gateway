package rabbit.gateway.common.bean;

import rabbit.gateway.common.Weight;

public class Target implements Weight {

    // 服务地址
    private String host;

    // 服务端口
    private int port;

    // 权重
    private long weight = 1;

    /**
     * ca证书
     */
    private String caCertificate;

    /**
     * 证书
     */
    private String certificate;

    public Target() {
    }

    public Target(String host, int port, long weight) {
        this();
        this.host = host;
        this.port = port;
        this.weight = weight;
    }

    @Override
    public long getWeight() {
        return weight;
    }

    @Override
    public void setWeight(long weight) {
        this.weight = weight;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCaCertificate() {
        return caCertificate;
    }

    public void setCaCertificate(String caCertificate) {
        this.caCertificate = caCertificate;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }
}
