package co.paralleluniverse.galaxy;


import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Callable;
import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.telecontrol.Classpath;
import org.gridkit.vicluster.telecontrol.jvm.JvmProps;
import org.gridkit.vicluster.telecontrol.ssh.RemoteNodeProps;

/**
 *
 * @author eitan
 */
public class NanoCloudRemoteTest extends BaseCloudTest {
//    @Test
    public void test_distributed_hello_world__basic_example() throws InterruptedException {
        cloud = CloudFactory.createSimpleSshCloud();
        cloud.node("localhost");
        String cachePath = "/tmp/cache";
        RemoteNodeProps.at(cloud.node("**")).setRemoteJavaExec("java").setRemoteJarCachePath(cachePath);
        JvmProps.at(cloud.node("**")).addJvmArg("-javaagent:" + cachePath + File.separatorChar + getRemotePathToJar("jatest"));

        cloud.node("**").touch();
        cloud.node("**").exec(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                String jvmName = ManagementFactory.getRuntimeMXBean().getName();
                System.out.println("My name is '" + jvmName + "'. Hello! file ");
                return null;
            }
        });
        Thread.sleep(300);
    }

    private static String getRemotePathToJar(final String partOfJarName) {
        for (Classpath.ClasspathEntry cpe : Classpath.getClasspath(ClassLoader.getSystemClassLoader()))
            if (cpe.getFileName().contains(partOfJarName))
                return File.separatorChar + cpe.getContentHash() + File.separatorChar + cpe.getFileName();
        throw new RuntimeException(partOfJarName + " not found in classpath");
    }
}