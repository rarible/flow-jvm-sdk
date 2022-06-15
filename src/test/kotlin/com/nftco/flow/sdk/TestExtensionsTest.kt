package com.nftco.flow.sdk

import com.nftco.flow.sdk.test.FlowServiceAccountCredentials
import com.nftco.flow.sdk.test.FlowTestAccount
import com.nftco.flow.sdk.test.FlowTestClient
import com.nftco.flow.sdk.test.FlowTestContractDeployment
import com.nftco.flow.sdk.test.TestAccount
import com.nftco.flow.sdk.test.TestContractArg
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

//@FlowEmulatorTest
@Disabled
class TestExtensionsTest {

    @FlowTestClient
    lateinit var accessAPI: FlowAccessApi

    @FlowTestClient
    lateinit var asyncAccessApi: AsyncFlowAccessApi

    @FlowServiceAccountCredentials
    lateinit var serviceAccount: TestAccount

    @FlowTestAccount(
        contracts = [
            FlowTestContractDeployment(
                name = "NonFungibleToken",
                codeFileLocation = "./flow/NonFungibleToken.cdc"
            )
        ]
    )
    lateinit var account0: TestAccount

    @FlowTestAccount(
        signAlgo = SignatureAlgorithm.ECDSA_P256,
        balance = 69.0,
        contracts = [
            FlowTestContractDeployment(
                name = "NothingContract",
                codeClasspathLocation = "/cadence/NothingContract.cdc",
                arguments = [
                    TestContractArg("name", "The Name"),
                    TestContractArg("description", "The Description"),
                ]
            )
        ]
    )
    lateinit var account1: TestAccount

    @FlowTestAccount(
        signAlgo = SignatureAlgorithm.ECDSA_SECP256k1,
        balance = 420.0,
        contracts = [
            FlowTestContractDeployment(
                name = "EmptyContract",
                code = "pub contract EmptyContract { init() { } }"
            )
        ]
    )
    lateinit var account2: TestAccount

    @FlowTestAccount(
        contracts = [
            FlowTestContractDeployment(
                name = "NonFungibleToken",
                codeFileLocation = "./flow/NonFungibleToken.cdc"
            ),
            FlowTestContractDeployment(
                name = "NothingContract",
                codeClasspathLocation = "/cadence/NothingContract.cdc",
                arguments = [
                    TestContractArg("name", "The Name"),
                    TestContractArg("description", "The Description"),
                ]
            ),
            FlowTestContractDeployment(
                name = "EmptyContract",
                code = "pub contract EmptyContract { init() { } }"
            )
        ]
    )
    lateinit var account3: TestAccount

    @FlowTestAccount
    lateinit var account4: TestAccount

    @Test
    fun `Test extensions work`() {
        accessAPI.ping()
        asyncAccessApi.ping().get()
        assertTrue(serviceAccount.isValid)
        assertTrue(account0.isValid)
        assertTrue(account1.isValid)
        assertTrue(account2.isValid)
        assertTrue(account3.isValid)
        assertTrue(account4.isValid)

        val addresses = setOf(
            account0.flowAddress,
            account1.flowAddress,
            account2.flowAddress,
            account3.flowAddress,
            account4.flowAddress
        )
        assertEquals(5, addresses.size)
    }
}
