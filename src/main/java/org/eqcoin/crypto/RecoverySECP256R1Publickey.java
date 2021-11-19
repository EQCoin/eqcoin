/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 *
 * @copyright 2018-present EQcoin Planet All rights reserved...
 * Copyright of all works released by EQcoin Planet or jointly released by
 * EQcoin Planet with cooperative partners are owned by EQcoin Planet
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Planet reserves all rights to take
 * any legal action and pursue any right or remedy available under applicable
 * law.
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
package org.eqcoin.crypto;

/**
 * @author Xun Wang
 * @date Apr 14, 2020
 * @email 10509759@qq.com
 */
public class RecoverySECP256R1Publickey extends RecoveryECCPublickey {

	static {
		recoverySECP256R1Publickey = new RecoverySECP256R1Publickey();
	}

	public static RecoverySECP256R1Publickey getInstance() {
		return recoverySECP256R1Publickey;
	}

	private static RecoverySECP256R1Publickey recoverySECP256R1Publickey;

	private RecoverySECP256R1Publickey() {
		ecdsaCurve = SECP256R1Curve.getInstance();
	}

}
