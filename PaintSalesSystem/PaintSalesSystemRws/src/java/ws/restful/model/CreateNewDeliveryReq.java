/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws.restful.model;

import entity.Delivery;

/**
 *
 * @author CHEN BINGSEN
 */
public class CreateNewDeliveryReq {

    public Delivery getDelivery() {
        return delivery;
    }

    public CreateNewDeliveryReq() {
    }

    public CreateNewDeliveryReq(Delivery delivery) {
        this.delivery = delivery;
    }

    
    
    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }
    
    private Delivery delivery;
    
    
    
}
