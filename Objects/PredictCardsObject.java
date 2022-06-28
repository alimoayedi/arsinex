package com.arsinex.com.Objects;

import com.arsinex.com.Utilities.Utils;

import java.text.ParseException;

public class PredictCardsObject {

    private int type;
    private String id, market, last_price, start_date, end_date, payout_up, payout_down;
    private String price_pool, vote_result, created_date, update_date, locked_price, percent, is_award_gone;

    /**
     *
     * @param type
     * @param id
     * @param market
     * @param last_price
     * @param start_date
     * @param end_date
     * @param payout_up
     * @param payout_down
     * @param price_pool
     * @param vote_result
     * @param created_date
     * @param update_date
     * @param locked_price
     * @param percent
     * @param is_award_gone
     */

    public PredictCardsObject(int type, String id, String market, String last_price, String start_date, String end_date, String payout_up, String payout_down, String price_pool, String vote_result, String created_date, String update_date, String locked_price, String percent, String is_award_gone) {
        this.type = type;
        this.id = id;
        this.market = market;
        this.last_price = last_price;
        this.start_date = start_date;
        this.end_date = end_date;
        this.payout_up = payout_up;
        this.payout_down = payout_down;
        this.price_pool = price_pool;
        this.vote_result = vote_result;
        this.created_date = created_date;
        this.update_date = update_date;
        this.locked_price = locked_price;
        this.percent = percent;
        this.is_award_gone = is_award_gone;
    }

    public int getType() {
        return type;
    }

    public String getId() {
        return id.equals("null") ? "#0" : "#" + id;
    }

    public String getMarket() {
        return market.equals("null") ? "XXXXXX" : market;
    }

    public String getLast_price() {
        return last_price.equals("null") ? "0" : last_price;
    }

    public long getStart_date() {
        if (start_date.equals("null")) {
            return 0;
        } else {
            Utils utils = new Utils();
            try {
                long time_in_second = utils.humanTimeToTimeStamp(start_date, "yyyy-MM-dd HH:mm:ss");
                return time_in_second;
            } catch (ParseException e) {
                return 0;
            }
        }
    }

    public long getEnd_date() {
        if (end_date.equals("null")) {
            return 0;
        } else {
            Utils utils = new Utils();
            try {
                long time_in_second = utils.humanTimeToTimeStamp(end_date, "yyyy-MM-dd HH:mm:ss");
                return time_in_second;
            } catch (ParseException e) {
                return 0;
            }
        }
    }

    public String getPayout_up() {
        return payout_up.equals("null") ? "0" : payout_up;
    }

    public String getPayout_down() {
        return payout_down.equals("null") ? "0" : payout_down;
    }

    public String getPrice_pool() {
        return price_pool.equals("null") ? "0" : price_pool;
    }

    public String getVote_result() {
        return vote_result.equals("null") ? "0" : vote_result;
    }

    public String getCreated_date() {
        return created_date.equals("null") ? "0" : created_date;
    }

    public String getUpdate_date() {
        return update_date.equals("null") ? "0" : update_date;
    }

    public String getLocked_price() {
        return locked_price.equals("null") ? "0" : locked_price;
    }

    public String getPercent() {
        Utils utils = new Utils();
        return percent.equals("null") ? "0" : utils.reduceDecimal(percent, 8);
    }

    public String getIs_award_gone() {
        return is_award_gone;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setLast_price(String last_price) {
        this.last_price = last_price;
    }
}
