package com.example.webservice.test.junit.web.controllers;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.webservice.domain.Account;
import com.example.webservice.domain.AccountTransaction;
import com.example.webservice.enums.TransactionType;
import com.example.webservice.rest.models.UserTransaction;
import com.example.webservice.shared.utils.AccountUtils;
import com.example.webservice.web.controllers.WithdrawalController;
import com.google.gson.Gson;

/**
 * @author 
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(WithdrawalController.class)
public class WithdrawalControllerTests extends BaseControllerTests {
	
	@Test
    public void testWithdrawalExceedsCurrentBalance() throws Exception {
		
		UserTransaction userTransaction = new UserTransaction(50000);
    	Gson gson = new Gson();
        String json = gson.toJson(userTransaction);
        
        given(this.accountService.findOne(1L)).willReturn(new Account(40000));
        
        this.mvc.perform(post("/withdrawal/").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andExpect(content().json("{\"success\":false,\"messages\":{\"message\":\"You have insufficient funds\",\"title\":\"Error\"},\"errors\":{},\"data\":{},\"httpResponseCode\":406}"));
		
	}
	
	@Test
    public void testMaxWithdrawalForTheDay() throws Exception {
		
		AccountTransaction transaction = new AccountTransaction(TransactionType.WITHDRAWAL.getId(), 40000, new Date());
    	AccountTransaction transaction2 = new AccountTransaction(TransactionType.WITHDRAWAL.getId(), 5000, new Date());
    	
    	List<AccountTransaction> list = new ArrayList<>();
    	list.add(transaction);
    	list.add(transaction2);
    	
    	UserTransaction userTransaction = new UserTransaction(8000);
    	Gson gson = new Gson();
        String json = gson.toJson(userTransaction);
        
        given(this.accountService.findOne(1L)).willReturn(new Account(400000));
        
        given(this.transactionsService.findByDateBetweenAndType(AccountUtils.getStartOfDay(new Date()),
                AccountUtils.getEndOfDay(new Date()), TransactionType.WITHDRAWAL.getId())).willReturn(list);
        
        this.mvc.perform(post("/withdrawal/").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andExpect(content().json("{\"success\":false,\"messages\":{\"message\":\"Withdrawal per day should not be more than $50K\",\"title\":\"Error\"},\"errors\":{},\"data\":{},\"httpResponseCode\":406}"));
		
	}
	
	@Test
    public void testMaxWithdrawalPerTransaction() throws Exception {
		
    	AccountTransaction transaction = new AccountTransaction(TransactionType.WITHDRAWAL.getId(), 5000, new Date());
    	AccountTransaction transaction2 = new AccountTransaction(TransactionType.WITHDRAWAL.getId(), 7500, new Date());
    	
    	List<AccountTransaction> list = new ArrayList<>();
    	list.add(transaction);
    	list.add(transaction2);
    	
    	UserTransaction userTransaction = new UserTransaction(25000);
    	Gson gson = new Gson();
        String json = gson.toJson(userTransaction);
        
        given(this.accountService.findOne(1L)).willReturn(new Account(400000));
        
        given(this.transactionsService.findByDateBetweenAndType(AccountUtils.getStartOfDay(new Date()),
                AccountUtils.getEndOfDay(new Date()), TransactionType.WITHDRAWAL.getId())).willReturn(list);
        
        this.mvc.perform(post("/withdrawal/").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andExpect(content().json("{\"success\":false,\"messages\":{\"message\":\"Exceeded Maximum Withdrawal Per Transaction\",\"title\":\"Error\"},\"errors\":{},\"data\":{},\"httpResponseCode\":406}"));
		
	}
	
	@Test
    public void testMaxAllowedWithdrawalPerDay() throws Exception {
		
    	AccountTransaction transaction = new AccountTransaction(TransactionType.WITHDRAWAL.getId(), 5000, new Date());
    	AccountTransaction transaction2 = new AccountTransaction(TransactionType.WITHDRAWAL.getId(), 7500, new Date());
    	AccountTransaction transaction3 = new AccountTransaction(TransactionType.WITHDRAWAL.getId(), 10500, new Date());
    	
    	List<AccountTransaction> list = new ArrayList<>();
    	list.add(transaction);
    	list.add(transaction2);
    	list.add(transaction3);
    	
    	UserTransaction userTransaction = new UserTransaction(1000);
    	Gson gson = new Gson();
        String json = gson.toJson(userTransaction);
        
        given(this.accountService.findOne(1L)).willReturn(new Account(400000));
        
        given(this.transactionsService.findByDateBetweenAndType(AccountUtils.getStartOfDay(new Date()),
                AccountUtils.getEndOfDay(new Date()), TransactionType.WITHDRAWAL.getId())).willReturn(list);
        
        this.mvc.perform(post("/withdrawal/").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andExpect(content().json("{\"success\":false,\"messages\":{\"message\":\"Maximum Withdrawal transactions for the day Exceeded\",\"title\":\"Error\"},\"errors\":{},\"data\":{},\"httpResponseCode\":406}"));
		
	}
	
	@Test
    public void testSuccessfulWithdrawal() throws Exception {
		
    	AccountTransaction transaction = new AccountTransaction(TransactionType.WITHDRAWAL.getId(), 5000, new Date());
    	AccountTransaction transaction2 = new AccountTransaction(TransactionType.WITHDRAWAL.getId(), 7500, new Date());
    	
    	List<AccountTransaction> list = new ArrayList<>();
    	list.add(transaction);
    	list.add(transaction2);
    	
    	UserTransaction userTransaction = new UserTransaction(1000);
    	Gson gson = new Gson();
        String json = gson.toJson(userTransaction);
        
        given(this.accountService.findOne(1L)).willReturn(new Account(70000));  
        
        given(this.transactionsService.findByDateBetweenAndType(AccountUtils.getStartOfDay(new Date()),
                AccountUtils.getEndOfDay(new Date()), TransactionType.WITHDRAWAL.getId())).willReturn(list);
        
        when(this.transactionsService.save(any(AccountTransaction.class))).thenReturn(transaction);
        when(this.accountService.save(any(Account.class))).thenReturn(new Account(400));
        
        this.mvc.perform(post("/withdrawal/").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andExpect(content().json("{\"success\":true,\"messages\":{\"message\":\"Withdrawal sucessfully Transacted\",\"title\":\"\"},\"errors\":{},\"data\":{},\"httpResponseCode\":200}"));
		
	}	

}
