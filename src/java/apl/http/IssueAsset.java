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
import apl.Attachment;
import apl.Constants;
import apl.AplException;
import apl.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static apl.http.JSONResponses.INCORRECT_ASSET_DESCRIPTION;
import static apl.http.JSONResponses.INCORRECT_ASSET_NAME;
import static apl.http.JSONResponses.INCORRECT_ASSET_NAME_LENGTH;
import static apl.http.JSONResponses.INCORRECT_DECIMALS;
import static apl.http.JSONResponses.MISSING_NAME;

public final class IssueAsset extends CreateTransaction {

    static final IssueAsset instance = new IssueAsset();

    private IssueAsset() {
        super(new APITag[] {APITag.AE, APITag.CREATE_TRANSACTION}, "name", "description", "quantityQNT", "decimals");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws AplException {

        String name = req.getParameter("name");
        String description = req.getParameter("description");
        String decimalsValue = Convert.emptyToNull(req.getParameter("decimals"));

        if (name == null) {
            return MISSING_NAME;
        }

        name = name.trim();
        if (name.length() < Constants.MIN_ASSET_NAME_LENGTH || name.length() > Constants.MAX_ASSET_NAME_LENGTH) {
            return INCORRECT_ASSET_NAME_LENGTH;
        }
        String normalizedName = name.toLowerCase();
        for (int i = 0; i < normalizedName.length(); i++) {
            if (Constants.ALPHABET.indexOf(normalizedName.charAt(i)) < 0) {
                return INCORRECT_ASSET_NAME;
            }
        }

        if (description != null && description.length() > Constants.MAX_ASSET_DESCRIPTION_LENGTH) {
            return INCORRECT_ASSET_DESCRIPTION;
        }

        byte decimals = 0;
        if (decimalsValue != null) {
            try {
                decimals = Byte.parseByte(decimalsValue);
                if (decimals < 0 || decimals > 8) {
                    return INCORRECT_DECIMALS;
                }
            } catch (NumberFormatException e) {
                return INCORRECT_DECIMALS;
            }
        }

        long quantityQNT = ParameterParser.getQuantityQNT(req);
        Account account = ParameterParser.getSenderAccount(req);
        Attachment attachment = new Attachment.ColoredCoinsAssetIssuance(name, description, quantityQNT, decimals);
        return createTransaction(req, account, attachment);

    }

}