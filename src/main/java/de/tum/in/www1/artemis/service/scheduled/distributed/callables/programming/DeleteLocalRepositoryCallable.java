package de.tum.in.www1.artemis.service.scheduled.distributed.callables.programming;

import java.util.concurrent.Callable;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.spring.context.SpringAware;

import de.tum.in.www1.artemis.domain.VcsRepositoryUrl;
import de.tum.in.www1.artemis.service.connectors.GitService;

@SpringAware
public class DeleteLocalRepositoryCallable implements Callable<Void>, java.io.Serializable {

    private transient GitService gitService;

    private final VcsRepositoryUrl repoUrl;

    public DeleteLocalRepositoryCallable(VcsRepositoryUrl repoUrl) {
        this.repoUrl = repoUrl;
    }

    @Override
    public Void call() throws GitAPIException {
        gitService.deleteLocalRepository(repoUrl);
        return null;
    }

    @Autowired
    public void setGitService(final GitService gitService) {
        this.gitService = gitService;
    }
}
