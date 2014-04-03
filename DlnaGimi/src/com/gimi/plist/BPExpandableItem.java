/*
 * Copyright 2011 Daniel Rendall
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gimi.plist;

/**
 * Base class for BPItems which are 'expandable' i.e. the collection classes.
 */
abstract class BPExpandableItem extends BPItem {

    protected final static int[] EMPTY = new int[0];
    private boolean expanded = false;

    @Override
    final void expand(BinaryPlistDecoder decoder) throws BinaryPlistException {
        if (!expanded) {
            doExpand(decoder);
            expanded = true;
        }
    }

    @Override
    final boolean canBeRoot() {
        return true;
    }

    protected abstract void doExpand(BinaryPlistDecoder decoder) throws BinaryPlistException;

    @Override
    final boolean isExpanded() {
        return expanded;
    }
}
