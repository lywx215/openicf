/*
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * U.S. Government Rights - Commercial software. Government users 
 * are subject to the Sun Microsystems, Inc. standard license agreement
 * and applicable provisions of the FAR and its supplements.
 * 
 * Use is subject to license terms.
 * 
 * This distribution may include materials developed by third parties.
 * Sun, Sun Microsystems, the Sun logo, Java and Project Identity 
 * Connectors are trademarks or registered trademarks of Sun 
 * Microsystems, Inc. or its subsidiaries in the U.S. and other
 * countries.
 * 
 * UNIX is a registered trademark in the U.S. and other countries,
 * exclusively licensed through X/Open Company, Ltd. 
 * 
 * -----------
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License(CDDL) (the License).  You may not use this file
 * except in  compliance with the License. 
 * 
 * You can obtain a copy of the License at
 * http://identityconnectors.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * -----------
 */
using System;
using System.Collections.Generic;
using System.Reflection;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Script;
using NUnit.Framework;
using NUnit.Framework.SyntaxHelpers;

namespace FrameworkTests
{
    /// <summary>
    /// Description of ScriptTests.
    /// </summary>
    [TestFixture]
    public class ScriptTests
    {
        [Test]
        public void testBooScripting() {
            ScriptExecutorFactory factory = ScriptExecutorFactory.NewInstance("BOO");
            ScriptExecutor exe = factory.NewScriptExecutor(new Assembly[0],"x", false);
            IDictionary<string, object> vals = new Dictionary<string, object>();
            vals["x"] = 1;
            Assert.AreEqual(1, exe.Execute(vals));
            vals["x"] = 2;
            Assert.AreEqual(2, exe.Execute(vals));
        }
        [Test]
        public void testShellScripting() {
            ScriptExecutorFactory factory = ScriptExecutorFactory.NewInstance("Shell");
            ScriptExecutor exe = factory.NewScriptExecutor(new Assembly[0],"echo bob", false);
            IDictionary<string, object> vals = new Dictionary<string, object>();
            Assert.AreEqual(0, exe.Execute(vals));
        }
        [Test]
        [ExpectedException(typeof(ArgumentException))]
        public void testUnsupported() {
            ScriptExecutorFactory.NewInstance("fadsflkj");
        }
    }
}
