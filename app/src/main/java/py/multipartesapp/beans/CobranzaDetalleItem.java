package py.multipartes2.beans;

/**
 * Created by Adolfo on 08/12/2016.
 */
public class CobranzaDetalleItem {

     String payment_type;
     Integer amount;
     String bank;
     String check_number;
     String expired_date;
     String check_name;
     String depositslip;


    public String getPayment_type() {
        return payment_type;
    }

    public void setPayment_type(String payment_type) {
        this.payment_type = payment_type;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getCheck_number() {
        return check_number;
    }

    public void setCheck_number(String check_number) {
        this.check_number = check_number;
    }

    public String getExpired_date() {
        return expired_date;
    }

    public void setExpired_date(String expired_date) {
        this.expired_date = expired_date;
    }

    public String getCheck_name() {
        return check_name;
    }

    public void setCheck_name(String check_name) {
        this.check_name = check_name;
    }

    public String getDepositslip() {
        return depositslip;
    }

    public void setDepositslip(String depositslip) {
        this.depositslip = depositslip;
    }
}
