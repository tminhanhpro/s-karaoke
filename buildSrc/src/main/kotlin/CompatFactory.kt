import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

private const val COMPAT = "vn/savify/karaoke/Compat"

/**
 * Thay cac loi goi API Java moi (Android 13+) bang ham tuong duong trong vn.savify.karaoke.Compat,
 * ap dung cho CA thu vien (NewPipeExtractor...) de app chay duoc tren Android TV box cu.
 */
abstract class CompatFactory : AsmClassVisitorFactory<InstrumentationParameters.None> {

    override fun isInstrumentable(classData: ClassData): Boolean =
        classData.className != "vn.savify.karaoke.Compat"

    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor =
        object : ClassVisitor(Opcodes.ASM9, nextClassVisitor) {
            override fun visitMethod(
                access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?
            ): MethodVisitor {
                val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
                return object : MethodVisitor(Opcodes.ASM9, mv) {
                    override fun visitMethodInsn(
                        opcode: Int, owner: String?, mName: String?, desc: String?, isInterface: Boolean
                    ) {
                        if (opcode == Opcodes.INVOKESTATIC && owner == "java/net/URLEncoder" && mName == "encode" &&
                            desc == "(Ljava/lang/String;Ljava/nio/charset/Charset;)Ljava/lang/String;"
                        ) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, COMPAT, "encode", desc, false)
                            return
                        }
                        if (opcode == Opcodes.INVOKESTATIC && owner == "java/net/URLDecoder" && mName == "decode" &&
                            desc == "(Ljava/lang/String;Ljava/nio/charset/Charset;)Ljava/lang/String;"
                        ) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, COMPAT, "decode", desc, false)
                            return
                        }
                        if (opcode == Opcodes.INVOKEVIRTUAL && owner == "java/lang/String") {
                            if (mName == "isBlank" && desc == "()Z") {
                                super.visitMethodInsn(Opcodes.INVOKESTATIC, COMPAT, "isBlank", "(Ljava/lang/String;)Z", false)
                                return
                            }
                            if (mName == "strip" && desc == "()Ljava/lang/String;") {
                                super.visitMethodInsn(Opcodes.INVOKESTATIC, COMPAT, "strip", "(Ljava/lang/String;)Ljava/lang/String;", false)
                                return
                            }
                            if (mName == "repeat" && desc == "(I)Ljava/lang/String;") {
                                super.visitMethodInsn(Opcodes.INVOKESTATIC, COMPAT, "repeat", "(Ljava/lang/String;I)Ljava/lang/String;", false)
                                return
                            }
                        }
                        super.visitMethodInsn(opcode, owner, mName, desc, isInterface)
                    }
                }
            }
        }
}
