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

package apl.http;

import apl.Account;
import apl.AplException;
import apl.crypto.Crypto;
import apl.util.Convert;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetSharedKey extends APIServlet.APIRequestHandler {

    static final GetSharedKey instance = new GetSharedKey();

    private GetSharedKey() {
        super(new APITag[] {APITag.MESSAGES}, "account", "secretPhrase", "nonce");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws AplException {

        String secretPhrase = ParameterParser.getSecretPhrase(req, true);
        byte[] nonce = ParameterParser.getBytes(req, "nonce", true);
        long accountId = ParameterParser.getAccountId(req, "account", true);
        byte[] publicKey = Account.getPublicKey(accountId);
        if (publicKey == null) {
            return JSONResponses.INCORRECT_ACCOUNT;
        }
        byte[] sharedKey = Crypto.getSharedKey(Crypto.getPrivateKey(secretPhrase), publicKey, nonce);
        JSONObject response = new JSONObject();
        response.put("sharedKey", Convert.toHexString(sharedKey));
        return response;

    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

}