package me.rkfg.xmpp.bot.nxt;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import me.rkfg.xmpp.bot.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import ru.ppsrk.gwt.client.LogicException;

public class NXTAPI {

    private static final long NQT = 100000000;
    private String apiBase;
    private String accountAddress;
    private String accountPass;

    public NXTAPI(String apiBase, String accountAddress, String accountPass) {
        super();
        this.apiBase = apiBase;
        this.accountAddress = accountAddress;
        this.accountPass = accountPass;
    }

    public List<Transaction> getTransactionsSince(Long since, Long confirmations) throws LogicException {
        List<Transaction> transactions = new LinkedList<Transaction>();
        URIBuilder uriBuilder = getURIBuilder("getAccountTransactions");
        uriBuilder.addParameter("account", accountAddress);
        uriBuilder.addParameter("type", "0");
        uriBuilder.addParameter("timestamp", since.toString());
        uriBuilder.addParameter("numberOfConfirmations", confirmations.toString());
        JSONObject jsonObject = getJSONResponse(uriBuilder);
        JSONArray transactionsJSON = jsonObject.optJSONArray("transactions");
        if (transactionsJSON != null) {
            for (int i = 0; i < transactionsJSON.length(); i++) {
                JSONObject transactionJSON = transactionsJSON.optJSONObject(i);
                if (transactionJSON != null) {
                    String txid = transactionJSON.optString("transaction");
                    Long timestamp = transactionJSON.optLong("blockTimestamp");
                    Long amount = transactionJSON.optLong("amountNQT") / NQT;
                    if (amount != null && timestamp != null && txid != null) {
                        transactions.add(new Transaction(txid, timestamp, amount));
                    }
                }
            }
        }
        return transactions;
    }

    public String getMessage(String txid) throws LogicException {
        URIBuilder uriBuilder = getURIBuilder("readMessage");
        uriBuilder.addParameter("transaction", txid);
        uriBuilder.addParameter("secretPhrase", accountPass);
        JSONObject jsonResponse = getJSONResponse(uriBuilder);
        String message = jsonResponse.optString("message");
        if (!message.isEmpty()) {
            return message;
        }
        return jsonResponse.optString("decryptedMessage");
    }

    private JSONObject getJSONResponse(URIBuilder uriBuilder) throws LogicException {
        try {
            HttpGet req = new HttpGet(uriBuilder.build());
            HttpResponse response = Utils.getHTTPClient().execute(req);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new LogicException("API unavailable");
            }
            JSONObject jsonObject = new JSONObject(Utils.readHttpResponse(response));
            return jsonObject;
        } catch (URISyntaxException e) {
            throw new LogicException("Invalid URI syntax.", e);
        } catch (ClientProtocolException e) {
            throw new LogicException("Client protocol exception.", e);
        } catch (IOException e) {
            throw new LogicException("IO exception.", e);
        }
    }

    private URIBuilder getURIBuilder(String requestType) throws LogicException {
        try {
            URIBuilder uriBuilder = new URIBuilder(apiBase + "/nxt");
            uriBuilder.addParameter("requestType", requestType);
            return uriBuilder;
        } catch (URISyntaxException e) {
            throw new LogicException("Invalid URI syntax", e);
        }
    }
}
