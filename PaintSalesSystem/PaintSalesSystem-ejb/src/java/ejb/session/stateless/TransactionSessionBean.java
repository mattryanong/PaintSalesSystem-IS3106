/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Customer;
import entity.Transaction;
import entity.TransactionLineItem;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import util.exception.CreateNewTransactionException;
import util.exception.CustomerNotFoundException;
import util.exception.CustomerTransactionNotFound;
import javax.annotation.Resource;
import javax.ejb.EJBContext;

/**
 *
 * @author Ko Jia Le
 */
@Stateless
public class TransactionSessionBean implements TransactionSessionBeanLocal {

    @EJB
    private CustomerEntitySessionBeanLocal customerEntitySessionBeanLocal;

    @PersistenceContext(unitName = "PaintSalesSystem-ejbPU")
    private EntityManager em;

    @Resource
    private EJBContext eJBContext;
    
    
//    @Override
//    public Transaction createTransaction(Customer customer, List<TransactionLineItem> transactionLineItems) throws CreateNewTransactionException {
//        Customer customerToUpdate;
//        try {
//            customerToUpdate = customerEntitySessionBeanLocal.retrieveCustomerByCustomerId(customer.getCustomerId());
//        } catch (CustomerNotFoundException ex) {
//            throw new CreateNewTransactionException("Account no valid to make a purchase. Please ensure you are logged in");
//        }
//
//        Transaction newTransaction = new Transaction();
//        for (TransactionLineItem lineItem : transactionLineItems) {
//            newTransaction.getTransactionLineItems().add(lineItem);
//        }
//
//        newTransaction.setCustomer(customerToUpdate);
//        em.persist(newTransaction);
//        em.flush();
//        customerToUpdate.getTransactions().add(newTransaction);
//
//        return newTransaction;
//    }

    @Override
    public List<Transaction> retrieveAllTransactionByUser(Long custId) throws CustomerTransactionNotFound {
        Customer customer;
        try {
            customer = customerEntitySessionBeanLocal.retrieveCustomerByCustomerId(custId);
        } catch (CustomerNotFoundException customerNotFoundException) {
            throw new CustomerTransactionNotFound("User Id provided is invalid");
        }

        List<Transaction> customerTransactions = customer.getTransactions();
        for (Transaction t : customerTransactions) {
            t.getTransactionLineItems().size(); //Not sure if this lazy loading works. Cause "2" relationship away
        }

        return customerTransactions;
    }

    @Override
    public Transaction retrieveIndividualTransactionByUser(Long custId, Long transactionId) throws CustomerTransactionNotFound {
        try {
            List<Transaction> transactions = retrieveAllTransactionByUser(custId);

            for (Transaction t : transactions) {
                if (t.getTransactionId().equals(transactionId)) {
                    t.getTransactionLineItems().size();
                    return t; //should i retrieve all the info by paint service/paint/delivery transactoin here? Then let client side to determine what data they want to show
                }
            }
            throw new CustomerTransactionNotFound("The customer's individual transaction is not found");
        } catch (CustomerTransactionNotFound customerTransactionNotFound) {
            throw customerTransactionNotFound;
        }
    }

    @Override
    public List<Transaction> retrieveAllTransactionByAdmin() {
        Query query = em.createQuery("SELECT t FROM Transaction t");
        List<Transaction> transactions = query.getResultList();
        for (Transaction t : transactions) {
            t.getTransactionLineItems().size(); //Not sure if this lazy loading works. Cause "2" relationship away
        }
        return transactions;
    }

    @Override
    public Transaction createNewTransaction(Transaction newTransaction, Long customerId) throws CustomerNotFoundException, CreateNewTransactionException {
        if (newTransaction != null) {
            try {
                Customer customer = customerEntitySessionBeanLocal.retrieveCustomerByCustomerId(customerId);
                newTransaction.setCustomer(customer);
                customer.getTransactions().add(newTransaction);

                em.persist(newTransaction);

                for (TransactionLineItem transactionLineItem : newTransaction.getTransactionLineItems()) {
                    //debit quantity
                    em.persist(transactionLineItem);
                }

                em.flush();
                return newTransaction;
            } catch (Exception ex) {
                eJBContext.setRollbackOnly();
                throw new CreateNewTransactionException(ex.getMessage());
            }
        } else {
            throw new CreateNewTransactionException("Transaction information not provided");
        }
    }
}
