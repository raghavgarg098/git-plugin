package hudson.plugins.git.extensions.impl;

import com.google.common.base.Function;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Build;
import hudson.model.Descriptor;
import hudson.model.Run;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.Objects;

public class SparseCheckoutPath extends AbstractDescribableImpl<SparseCheckoutPath> implements Serializable {

    private static final long serialVersionUID = -6177158367915899356L;

    @SuppressFBWarnings(value="SE_TRANSIENT_FIELD_NOT_RESTORED", justification="Default value is OK in deserialization")

    private final String path;

    @DataBoundConstructor
    public SparseCheckoutPath(String path) {
        this.path = path;
    }

    @Whitelisted
    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SparseCheckoutPath that = (SparseCheckoutPath) o;

        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path);
    }

    @Override
    public String toString() {
        return path;
    }

    public static class SparseCheckoutPathToPath implements Function<SparseCheckoutPath, String>, Serializable {
        @Nullable
        private Run<?, ?> build;

        SparseCheckoutPathToPath(Run<?, ?> build) {
            this.build = build;
        }

        public String apply(@NonNull SparseCheckoutPath sparseCheckoutPath) {
            String path = sparseCheckoutPath.getPath();
            if (build == null) {
                return path;
            }

            try {
                EnvVars envVars = build.getEnvironment();
                Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
                System.out.println("Path: " + path);
                Matcher matcher = pattern.matcher(path);
                if (matcher.find()) {
                    String envKey = matcher.group(1);
                    String value = envVars.get(envKey, "");
                    System.out.println("Value: " + value);
                    path = path.replace("${" + envKey + "}", value);
                }
            } catch (InterruptedException | IOException e) {
            }
            return path;
        }
    }

    public Descriptor<SparseCheckoutPath> getDescriptor()
    {
        return Jenkins.get().getDescriptor(getClass());
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<SparseCheckoutPath> {
        @Override
        public String getDisplayName() { return "Path"; }
    }
}
