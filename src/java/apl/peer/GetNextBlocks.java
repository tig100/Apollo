/*
 * Copyright © 2013-2016 The Apl Core Developers.
 * Copyright © 2016-2017 Apollo Foundation IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Apollo Foundation B.V.,
 * no part of the Apl software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package apl.peer;

import apl.Block;
import apl.Apl;
import apl.util.Convert;
import apl.util.JSON;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import java.util.ArrayList;
import java.util.List;

final class GetNextBlocks extends PeerServlet.PeerRequestHandler {

    static final GetNextBlocks instance = new GetNextBlocks();

    static final JSONStreamAware TOO_MANY_BLOCKS_REQUESTED;
    static {
        JSONObject response = new JSONObject();
        response.put("error", Errors.TOO_MANY_BLOCKS_REQUESTED);
        TOO_MANY_BLOCKS_REQUESTED = JSON.prepare(response);
    }

    private GetNextBlocks() {}


    @Override
    JSONStreamAware processRequest(JSONObject request, Peer peer) {

        JSONObject response = new JSONObject();
        JSONArray nextBlocksArray = new JSONArray();
        List<? extends Block> blocks;
        long blockId = Convert.parseUnsignedLong((String) request.get("blockId"));
        List<String> stringList = (List<String>)request.get("blockIds");
        if (stringList != null) {
            if (stringList.size() > 36) {
                return TOO_MANY_BLOCKS_REQUESTED;
            }
            List<Long> idList = new ArrayList<>();
            stringList.forEach(stringId -> idList.add(Convert.parseUnsignedLong(stringId)));
            blocks = Apl.getBlockchain().getBlocksAfter(blockId, idList);
        } else {
            long limit = Convert.parseLong(request.get("limit"));
            if (limit > 36) {
                return TOO_MANY_BLOCKS_REQUESTED;
            }
            blocks = Apl.getBlockchain().getBlocksAfter(blockId, limit > 0 ? (int)limit : 36);
        }
        blocks.forEach(block -> nextBlocksArray.add(block.getJSONObject()));
        response.put("nextBlocks", nextBlocksArray);

        return response;
    }

    @Override
    boolean rejectWhileDownloading() {
        return true;
    }

}