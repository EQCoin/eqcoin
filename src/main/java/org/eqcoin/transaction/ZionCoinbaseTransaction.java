/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth Corporation All Rights Reserved...
 * Copyright of all works released by Wandering Earth Corporation or jointly
 * released by Wandering Earth Corporation with cooperative partners are owned
 * by Wandering Earth Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair
 * use or without prior written permission, Wandering Earth Corporation reserves
 * all rights to take any legal action and pursue any right or remedy available
 * under applicable law.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.eqcoin.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.lock.LockMate;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.passport.passport.AssetPassport;
import org.eqcoin.passport.passport.Passport;
import org.eqcoin.transaction.txout.TransferTxOut;
import org.eqcoin.transaction.txout.ZionTxOut;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date Mar 25, 2020
 * @email 10509759@qq.com
 */
public class ZionCoinbaseTransaction extends Transaction {
	private TransferTxOut eqCoinFederalTxOut;
	private ZionTxOut eqCoinMinerTxOut;
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.ZionTransaction#init()
	 */
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		super.init();
		transactionType = TransactionType.ZIONCOINBASE;
	}

	public ZionCoinbaseTransaction() {
		super();
	}

	public ZionCoinbaseTransaction(ByteArrayInputStream is)
			throws Exception {
		super(is);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.Transaction#isValid(com.eqzip.eqcoin.blockchain.
	 * AccountsMerkleTree)
	 */
	@Override
	public boolean isValid() throws NoSuchFieldException, IllegalStateException, IOException, Exception {
		if(!nonce.equals(eqcHive.getRoot().getHeight().getNextID())) {
			Log.Error("!nonce.equals(eqcHive.getRoot().getHeight().getNextID())");
			return false;
		}
		ID lockId = eqcHive.getGlobalState().isLockMateExists(eqCoinMinerTxOut.getLock());
		if(lockId != null) {
			Log.Error("Miner Coinbase Passport relevant Lock shouldn't exists.");
			return false;
		}
		Value minerCoinbaseReward = Util.getCurrentMinerCoinbaseReward(Util.getCurrentCoinbaseReward(eqcHive.getGlobalState()));
		if(!eqCoinFederalTxOut.getValue().equals(Util.getCurrentCoinbaseReward(eqcHive.getGlobalState()).subtract(minerCoinbaseReward))) {
			Log.Error("EQcoinFederal's coinbase reward is invalid");
			return false;
		}
		if(!eqCoinMinerTxOut.getValue().equals(minerCoinbaseReward)) {
			Log.Error("EQcoinMiner's coinbase reward is invalid");
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#isTransactionTypeSanity()
	 */
	@Override
	protected boolean isTransactionTypeSanity() {
		if(transactionType == null) {
			Log.Error("transactionType == null");
			return false;
		}
		if(transactionType != TransactionType.ZIONCOINBASE) {
			Log.Error("transactionType != TransactionType.ZIONCOINBASE");
			return false;
		}
		return true;
	}
	
	@Override
	protected boolean isStatusSanity() {
		if(priority != null) {
			Log.Error("priority != null");
			return false;
		}
		if(lockType != null) {
			Log.Error("lockType != null");
			return false;
		}
		return true;
	}

	@Override
	public boolean isDerivedSanity() throws Exception {
		if(eqCoinFederalTxOut == null) {
			Log.Error("eqCoinFederalTxOut == null");
			return false;
		}
		if(!(eqCoinFederalTxOut instanceof TransferTxOut)) {
			Log.Error("!(eqCoinFederalTxOut instanceof TransferTxOut)");
			return false;
		}
		if(!eqCoinFederalTxOut.isSanity()) {
			Log.Error("!eqCoinFederalTxOut.isSanity()");
			return false;
		}
		if(eqCoinMinerTxOut == null) {
			Log.Error("eqCoinMinerTxOut == null");
			return false;
		}
		if(!(eqCoinMinerTxOut instanceof ZionTxOut)) {
			Log.Error("!(eqCoinMinerTxOut instanceof ZionTxOut)");
			return false;
		}
		if(!eqCoinMinerTxOut.isSanity()) {
			Log.Error("!eqCoinMinerTxOut.isSanity()");
			return false;
		}
		if(!(eqCoinMinerTxOut.getValue().compareTo(Util.MIN_BALANCE) >= 0)) {
			Log.Error("!(eqCoinMinerTxOut.getValue().compareTo(Util.MIN_BALANCE) >= 0)");
			return false;
		}
		if(!eqCoinFederalTxOut.getPassportId().equals(ID.ZERO)) {
			Log.Error("!eqCoinFederalTxOut.getPassportId().equals(ID.ZERO)");
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#derivedPlanting()
	 */
	@Override
	protected void derivedPlanting() throws Exception {
		Passport passport = null;
		LockMate lock = null;
		// Planting CoinbaseTransaction's relevant Passport
		// Planting EQcoin Federal's Passport
		passport = eqcHive.getGlobalState().getPassport(eqCoinFederalTxOut.getPassportId());
		passport.deposit(eqCoinFederalTxOut.getValue());
		passport.setEQCHive(eqcHive).planting();
		// Planting Miner's Passport
		lock = new LockMate();
		lock.setLock(eqCoinMinerTxOut.getLock());
		lock.setId(eqcHive.getGlobalState().getLastLockMateId().getNextID());
		lock.setEQCHive(eqcHive).planting();
		passport = new AssetPassport();
		passport.setId(eqcHive.getGlobalState().getLastPassportId().getNextID());
		passport.setLockID(lock.getId());
		passport.deposit(eqCoinMinerTxOut.getValue());
		passport.setEQCHive(eqcHive).planting();
	}
	
	public String toInnerJson() {
		return
		"\"ZionCoinbaseTransaction\":" + "\n{\n"
		+ "\"EQcoinFederalTxOut\":" + "\n" + eqCoinFederalTxOut.toInnerJson() + "\n,"
		+ "\"EQcoinMinerTxOut\":" + "\n" + eqCoinMinerTxOut.toInnerJson() + "\n,"
		+ "\"Nonce\":" + "\"" + nonce + "\"" + 
		"\n}";
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#parseHeader(java.io.ByteArrayInputStream, com.eqcoin.blockchain.transaction.Transaction.TransactionShape)
	 */
	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		// Parse Transaction type
		transactionType = TransactionType.get(EQCCastle.parseID(is).intValue());
		// Parse nonce
		nonce = EQCCastle.parseID(is);
	}

	public void parseBody(ByteArrayInputStream is) throws Exception {
		eqCoinFederalTxOut = new TransferTxOut(is);
		eqCoinMinerTxOut = new ZionTxOut(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#getHeaderBytes(com.eqcoin.blockchain.transaction.Transaction.TransactionShape)
	 */
	@Override
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		// Serialization Transaction type
		os.write(transactionType.getEQCBits());
		// Serialization nonce
		os.write(EQCCastle.bigIntegerToEQCBits(nonce));
		return os;
	}

	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		// Serialization TxOut
		os.write(eqCoinFederalTxOut.getBytes());
		os.write(eqCoinMinerTxOut.getBytes());
		return os;
	}

	/**
	 * @return the eqCoinFederalTxOut
	 */
	public TransferTxOut getEqCoinFederalTxOut() {
		return eqCoinFederalTxOut;
	}

	/**
	 * @param eqCoinFederalTxOut the eqCoinFederalTxOut to set
	 */
	public void setEqCoinFederalTxOut(TransferTxOut eqCoinFederalTxOut) {
		this.eqCoinFederalTxOut = eqCoinFederalTxOut;
	}

	/**
	 * @return the eqCoinMinerTxOut
	 */
	public ZionTxOut getEqCoinMinerTxOut() {
		return eqCoinMinerTxOut;
	}

	/**
	 * @param eqCoinMinerTxOut the eqCoinMinerTxOut to set
	 */
	public void setEqCoinMinerTxOut(ZionTxOut eqCoinMinerTxOut) {
		this.eqCoinMinerTxOut = eqCoinMinerTxOut;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#isEQCWitnessSanity()
	 */
	@Override
	protected boolean isWitnessSanity() {
		if(witness != null) {
			Log.Error("eqcWitness != null");
			return false;
		}
		return true;
	}
	
	@Override
	public void init(EQCHive eqcHive) throws Exception {
		this.eqcHive = eqcHive;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#initPlanting()
	 */
	@Override
	protected boolean isMeetPreCondition() throws Exception {
		return true;
	}
	
}
