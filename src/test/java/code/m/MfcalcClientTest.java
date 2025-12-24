package code.m;

import com.ncslab.code.m.MfcalcClient;
import com.ncslab.dto.communication.MfcalcResponseDto;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;

public class MfcalcClientTest {

    @Test
    public void test() throws IOException {

        MfcalcClient client = new MfcalcClient(null);
        String mainCode = "% 定义一个矩阵\n"+
            "A = [1, 2; 3, 4]\n"+
            "% 计算矩阵的逆\n"+
            "inv_A = inv(A)\n"+
            "% 绘制正弦波\n"+
            "x = linspace(0, 2*pi, 100)\n"+
            "y = sn(x)\n"+
            "y = cos(x)\n";

        MfcalcResponseDto response = client.runScript(mainCode);
        System.out.println(response);

        response = client.runScript("y = A+1\n");
        System.out.println(response);

        response = client.runScript(mainCode);
        System.out.println(response);

        client.close();
    }
}
