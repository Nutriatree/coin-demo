package org.example.jiwoo_coin_demo.service;

import org.example.jiwoo_coin_demo.contracts.Jiwoo_coin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Value;
import org.web3j.crypto.Credentials; // 미리 추가
import org.web3j.tx.gas.DefaultGasProvider; // 미리 추가



// Notate Spring that this class is a component of Service layer.
@Service
public class EthereumService {

    // Inject Web3j Bean to we3j object.
    @Autowired
    private Web3j web3j;

    /**
     * Gets the latest block number.
     */
    public BigInteger getLatestBlockNumber() throws IOException {
        EthBlockNumber ethBlockNumber = web3j.ethBlockNumber().send();
        return ethBlockNumber.getBlockNumber();
    }

    /**
     * get ETH balance of specific wallet address.
     * @param address address of wallet.
     */
    public BigDecimal getEthBalance(String address) throws IOException {
        EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();

        // 3. translate the WEI to ETH
        BigInteger weiBalance = ethGetBalance.getBalance();
        return Convert.fromWei(new BigDecimal(weiBalance), Convert.Unit.ETHER);
    }

    @Value("${token.contract-address}")
    private String contractAddress; // 2단계에서 설정한 주소를 주입받습니다.

    // ... (기존 getLatestBlockNumber, getEthBalance 메소드)
    @Autowired
    private Credentials credentials;
    /**
     * 특정 주소의 ERC-20 토큰 잔액을 조회하는 메소드
     * @param ownerAddress 잔액을 조회할 지갑 주소
     * @return 토큰 잔액 (단위: Ether)
     */
    public BigDecimal getTokenBalance(String ownerAddress) throws Exception {
        // 1. 컨트랙트 로드: Wrapper 클래스를 이용해 컨트랙트 객체를 불러옵니다.
        // '읽기' 작업만 할 때는 Credentials 정보가 사실상 필요 없지만,
        // '쓰기' 작업과 호환성을 위해 미리 넣어줍니다.
        Jiwoo_coin tokenContract = Jiwoo_coin.load(contractAddress, web3j, credentials, new DefaultGasProvider());

        // 2. 컨트랙트의 balanceOf 함수 호출
        BigInteger balanceInWei = tokenContract.balanceOf(ownerAddress).send();

        // 3. 결과를 Ether 단위로 변환하여 반환
        return Convert.fromWei(new BigDecimal(balanceInWei), Convert.Unit.ETHER);
    }

    public String getTokenName() throws Exception {
        // Load the contract: load the contract object using Wrapper class(RaewooCoin).
        Jiwoo_coin tokenContract = Jiwoo_coin.load(contractAddress, web3j, credentials, new DefaultGasProvider());

        return tokenContract.name().send();
    }

    /**
     * Get full Symbol of ERC-20 Token.
     * @return Symobl of Token.
     */
    public String getTokenSymbol() throws Exception {
        // Load the contract: load the contract object using Wrapper class(RaewooCoin).
        Jiwoo_coin tokenContract = Jiwoo_coin.load(contractAddress, web3j, credentials, new DefaultGasProvider());

        return tokenContract.symbol().send();
    }

    /**
     * transfer ERC-20 token to specific address
     * @param toAddress address that receives the tokens
     * @param amount amount to send in unit ETH
     * @return transaction hash
     */
    public String sendToken(String toAddress, BigDecimal amount) throws Exception {
        // Load the contract: load the contract object using Wrapper class(RaewooCoin).
        Jiwoo_coin tokenContract = Jiwoo_coin.load(contractAddress, web3j, credentials, new DefaultGasProvider());

        // Translate ETH to Wei
        BigInteger amountInWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();

        // Call transfer function from contract, and send the token.
        // At the moment .send() is called, the transaction, signed with private key in application.properties, will be sent to Alchemy node.
        TransactionReceipt transactionReceipt = tokenContract.transfer(toAddress, amountInWei).send();

        // Return the transaction hash.
        return transactionReceipt.getTransactionHash();
    }
}
