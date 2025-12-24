package com.hualong;

import com.hualong.websocket.BlocklyJsonParser;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.utils.ResourceReader;

import java.util.List;
import java.util.Map;

public class BlocklyJsonParserTest {
    String filePath = "simulateWebsocket.json";//"websocketCompile.json"; // 替换为实际文件路径

    @Before
    public void setUp(){
//        SqlSession sqlSession = Mybatis1Utils.getSqlSession();
//        MdlBlockMapper mapper = sqlSession.getMapper( MdlBlockMapper.class);
//        mdlBlockList =  mapper.selectAll();
//        sqlSession.commit();
//        sqlSession.close();
    }

    @Test
    public void simulateOnce(){
        String filePath = "com/hualong/block/parser/block.json";//"websocketCompile.json"; // 替换为实际文件路径
        JSONObject jsonIn = new JSONObject(ResourceReader.readJsonResource(filePath));
        String msgString = jsonIn.toString();
        BlocklyJsonParser parser = new BlocklyJsonParser();
        List<BlocklyJsonParser.BlocklyBlock> blocks = parser.parseBlocklyJson(msgString);
        for(BlocklyJsonParser.BlocklyBlock block:blocks){
            System.out.println("Block Type: " + block.getType());
            System.out.println("Block ID: " + block.getId());
            System.out.println("Fields: " + block.getFields());
            System.out.println("Statements:\n" + block.getStatements());
            List<BlocklyJsonParser.BlocklyBlock> nextBlocks = block.getNextBlocks();
            for (BlocklyJsonParser.BlocklyBlock nextBlock : nextBlocks) {

            }
//            System.out.println("Next Blocks: " + block.getNextBlocks());
        }

        Map<String, Object> variables = parser.getVariableMap();
        System.out.println(variables);
    }

    @After
    public void finish(){

    }
}
