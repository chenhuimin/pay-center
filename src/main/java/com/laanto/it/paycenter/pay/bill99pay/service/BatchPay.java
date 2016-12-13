/**
 * BatchPay.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.laanto.it.paycenter.pay.bill99pay.service;

public interface BatchPay extends java.rmi.Remote {
    public com.laanto.it.paycenter.pay.bill99pay.pojo.QueryResponseBean[] queryDeal(com.laanto.it.paycenter.pay.bill99pay.pojo.QueryRequestBean input, java.lang.String username, java.lang.String ip) throws java.rmi.RemoteException;
    public com.laanto.it.paycenter.pay.bill99pay.pojo.BankResponseBean[] bankPay(com.laanto.it.paycenter.pay.bill99pay.pojo.BankRequestBean[] input, java.lang.String username, java.lang.String ip) throws java.rmi.RemoteException;
    public com.laanto.it.paycenter.pay.bill99pay.pojo.SimpleResponseBean[] simplePay(com.laanto.it.paycenter.pay.bill99pay.pojo.SimpleRequestBean[] input, java.lang.String username, java.lang.String ip) throws java.rmi.RemoteException;
    public com.laanto.it.paycenter.pay.bill99pay.pojo.PostResponseBean[] postPay(com.laanto.it.paycenter.pay.bill99pay.pojo.PostRequestBean[] input, java.lang.String username, java.lang.String ip) throws java.rmi.RemoteException;
}
