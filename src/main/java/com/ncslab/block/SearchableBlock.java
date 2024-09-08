package com.ncslab.block;

import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.BlockType;
import com.ncslab.ncslablink.NCSLabModel;

/**
 * New models ples extends this.
 * Ples put the key and value into the hashmap provided in <code>BlockType</code>.

 * <code><p>
 * class MyBlock extends SoughtedBlock{
 * </p>
 *  static{</p>
 *      BlockType.put(blockname, create);</p>
 *  }</p>
 *  static MyBlock create(JSONObject json, NCSLabModel model){</p>
 *      //your preprocess part</p>
 *      return new MyBlock(json, model);</p>
 *  }</p>
 * </code>
 * And this class is only in use to vertify whether the block is written in previous version or recent version.
 */
public class SearchableBlock extends Block{
    public SearchableBlock(JSONObject jsonObject, NCSLabModel model){
        super(jsonObject, model);
    }
}

/* Example */
class MyBlock extends SearchableBlock{
    static{
        BlockType.put("My Block", MyBlock::create);
    }


    public MyBlock(JSONObject jsonObject, NCSLabModel model){
        super(jsonObject, model);
    }

    static MyBlock create(JSONObject jsonObject, NCSLabModel model){
        return new MyBlock(jsonObject, model);
    }

}
