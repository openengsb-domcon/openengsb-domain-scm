/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.domain.scm;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.Domain;
import org.openengsb.core.api.Raises;

/**
 * ScmDomain is an abstraction for working with SCM tools.
 *
 */
public interface ScmDomain extends Domain {

    /**
     * Looks up changes in a remote repository and updates the local repository or checks out a new local repository and
     * returns a list of {@link CommitRef} with the revisions produced since the last update or <code>null</code>.
     */
    @Deprecated
    @Raises({ UpdateEvent.class })
    List<CommitRef> update();

    /**
     * Exports the files and directories of the HEAD revision from a repository without the SCM specific data as
     * ZIP-compressed data.
     */
    byte[] export();

    /**
     * Exports the files and directories of a revision identified by the {@link CommitRef} from a repository without the
     * SCM specific data as ZIP-compressed data.
     */
    byte[] export(CommitRef ref);

    /**
     * Check if file identified by its {@code fileName} exists in the HEAD revision and returns <code>true</code> if it
     * does.
     */
    boolean exists(String file);

    /**
     * Retrieves a single {@link File} from a repository identified by its {@code fileName} if it exists in the HEAD
     * revision. If the file does not exist in the revision <code>null</code> will be returned.
     */
    byte[] get(String file);

    /**
     * Check if file identified by its {@code fileName} exists in a revision identified by the {@link CommitRef} and
     * returns <code>true</code> if it does.
     */
    boolean exists(String fileName, CommitRef version);

    /**
     * Retrieves a single {@link File} from a repository identified by its {@code fileName} if it exists in the revision
     * identified by the {@link CommitRef}. If the file does not exist in the revision <code>null</code> will be
     * returned.
     */
    byte[] get(String fileName, CommitRef ref);

    /**
     * Returns the {@link CommitRef} of the current HEAD in the repository or <code>null</code>.
     */
    CommitRef getHead();

    /**
     * Adds a file with at the given location {@code path} and fills it with the given {@code content}. If the file
     * exists, it is overwritten. The changes are then committed with the given {@code comment}.
     *
     * Returns the {@link CommitRef} of the commit triggered.
     */
    CommitRef add(String comment, String path, byte[] content);

    /**
     * Adds a all files to the repository and commits the changes. The given {@link Map} contains the paths as keys and
     * their corresponding content as values. If a file exists, it is overwritten. The changes are then committed with
     * the given {@code comment}.
     *
     * Returns the {@link CommitRef} of the commit triggered.
     */
    CommitRef add(String comment, Map<String, byte[]> files);

    /**
     * Removes one or more files from the working directory of the repository and commits them with a passed
     * {@code comment}. Returns the {@link CommitRef} of the commit triggered.
     */
    CommitRef remove(String comment, String... path);

    /**
     * Tags the actual HEAD of the repository with the passed {@code tagName} and returns the corresponding
     * {@link TagRef} or <code>null</code>.
     */
    TagRef tagRepo(String tagName);

    /**
     * Tags the commit of the repository identified by the {@link CommitRef} with the passed {@code tagName} and returns
     * the corresponding {@link TagRef} or <code>null</code>.
     */
    TagRef tagRepo(String tagName, CommitRef ref);

    /**
     * Resolves and returns the {@link CommitRef} for a {@link TagRef} or <code>null</code> if the reference does not
     * exist.
     */
    CommitRef getCommitRefForTag(TagRef ref);

}
