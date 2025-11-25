package com.ncslab.block.sink;

import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.sink.SinkDto;

import com.ncslab.block.Block;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.NCSLabModel;

public class SinkBlock extends Block {
	public SinkBlock(JSONObject scopeIn,NCSLabModel model) {
		super(scopeIn,model);
	}
	
	/**
	 * DTO-NATIVE Constructor for SinkBlock - Uses DTO directly without conversion
	 */
	public SinkBlock(BlockDto blockDto, NCSLabModel model) {
		super(blockDto, model);
	}

	/**
	 * Factory method to create SinkBlock from BlockDto.
	 *
	 * @param dto The BlockDto containing block configuration
	 * @param model The NCSLabModel this block belongs to
	 * @return New SinkBlock instance
	 * @throws BlockCreationException if block creation fails
	 */
	public static SinkBlock createFromDto(BlockDto dto, NCSLabModel model) throws BlockCreationException {
		return new SinkBlock(dto, model);
	}
}
