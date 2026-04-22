import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncslab.dto.block.specialized.stateflow.StateflowChartDto;

public class TestDeser {
    public static void main(String[] args) throws Exception {
        String json = "{\"blockType\":\"StateflowChart\",\"blockName\":\"Chart1\",\"stateflowData\":{\"id\":\"chart-1\",\"name\":\"Chart1\",\"cells\":[],\"properties\":{\"stateMachineType\":\"Classic\"},\"variables\":[{\"name\":\"x\",\"dataType\":\"double\",\"scope\":\"input\"}],\"events\":[]}}";
        ObjectMapper mapper = new ObjectMapper();
        StateflowChartDto dto = mapper.readValue(json, StateflowChartDto.class);
        System.out.println("dto class: " + dto.getClass().getName());
        System.out.println("stateflowData: " + dto.getStateflowData());
        System.out.println("chartName: " + dto.getChartName());
        System.out.println("variables: " + dto.getVariables());
        System.out.println("events: " + dto.getEvents());
        System.out.println("properties: " + dto.getChartProperties());
    }
}
