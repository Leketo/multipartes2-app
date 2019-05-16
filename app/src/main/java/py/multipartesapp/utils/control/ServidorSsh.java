package py.multipartesapp.utils.control;

import android.util.Log;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.apache.sshd.server.shell.ProcessShellFactory;

import java.io.IOException;
import java.util.Arrays;

import py.multipartesapp.activities.Main;

public class ServidorSsh {

    public static final String TAG = Main.class.getSimpleName();

    public void startSSHServer() {
        int port = 8888;
        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(
                "src/test/resources/hostkey.ser"));
        sshd.setSubsystemFactories(Arrays
                .<NamedFactory<Command>> asList(new SftpSubsystem.Factory()));
        sshd.setCommandFactory(new ScpCommandFactory());
        sshd.setShellFactory(new ProcessShellFactory(new String[] { "/system/bin/sh", "-i", "-l" })); // necessary if you want to type commands over ssh
        sshd.setPasswordAuthenticator(new PasswordAuthenticator() {

            @Override
            public boolean authenticate(String u, String p, ServerSession s) {
                return ("sftptest".equals(u) && "sftptest".equals(p));
            }
        });

        try {
            sshd.start();
            Log.d(TAG,"Iniciando");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
