/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.cloud.storage.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.storage.SnapshotVO;
import com.cloud.storage.Snapshot.Type;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;

@Local (value={SnapshotDao.class})
public class SnapshotDaoImpl extends GenericDaoBase<SnapshotVO, Long> implements SnapshotDao {
    public static final Logger s_logger = Logger.getLogger(SnapshotDaoImpl.class.getName());
    private static final String GET_LAST_SNAPSHOT = "SELECT id FROM snapshots where volume_id = ? AND id != ? AND path IS NOT NULL ORDER BY created DESC";
    private static final String UPDATE_SNAPSHOT_VERSION = "UPDATE snapshots SET version = ? WHERE volume_id = ? AND version = ?";
    
    private final SearchBuilder<SnapshotVO> VolumeIdSearch;
    private final SearchBuilder<SnapshotVO> VolumeIdTypeSearch;
    private final SearchBuilder<SnapshotVO> ParentIdSearch;
    private final SearchBuilder<SnapshotVO> backupUuidSearch;   
    private final SearchBuilder<SnapshotVO> VolumeIdVersionSearch;
    
    @Override
    public SnapshotVO findNextSnapshot(long snapshotId) {
        SearchCriteria<SnapshotVO> sc = ParentIdSearch.create();
        sc.setParameters("prevSnapshotId", snapshotId);
        return findOneIncludingRemovedBy(sc);
    }
    
    @Override
     public List<SnapshotVO> listByBackupUuid(long volumeId, String backupUuid) {
        SearchCriteria<SnapshotVO> sc = backupUuidSearch.create();
        sc.setParameters("backupUuid", backupUuid);
        return listBy(sc, null);
    }
    
    @Override
    public List<SnapshotVO> listByVolumeIdType(long volumeId, Type type ) {
        return listByVolumeIdType(null, volumeId, type);
    }
    
    
    @Override
    public List<SnapshotVO> listByVolumeIdVersion(long volumeId, String version ) {
        return listByVolumeIdVersion(null, volumeId, version);
    }

    @Override
    public List<SnapshotVO> listByVolumeId(long volumeId) {
        return listByVolumeId(null, volumeId);
    }
    
    @Override
    public List<SnapshotVO> listByVolumeId(Filter filter, long volumeId ) {
        SearchCriteria<SnapshotVO> sc = VolumeIdSearch.create();
        sc.setParameters("volumeId", volumeId);
        return listBy(sc, filter);
    }
    
    @Override
    public List<SnapshotVO> listByVolumeIdIncludingRemoved(long volumeId) {
        SearchCriteria<SnapshotVO> sc = VolumeIdSearch.create();
        sc.setParameters("volumeId", volumeId);
        return listIncludingRemovedBy(sc, null);
    }
    
    public List<SnapshotVO> listByVolumeIdType(Filter filter, long volumeId, Type type ) {
        SearchCriteria<SnapshotVO> sc = VolumeIdTypeSearch.create();
        sc.setParameters("volumeId", volumeId);
        sc.setParameters("type", type.ordinal());
        return listBy(sc, filter);
    }
    
    public List<SnapshotVO> listByVolumeIdVersion(Filter filter, long volumeId, String version ) {
        SearchCriteria<SnapshotVO> sc = VolumeIdVersionSearch.create();
        sc.setParameters("volumeId", volumeId);
        sc.setParameters("version", version);
        return listBy(sc, filter);
    }

    protected SnapshotDaoImpl() {
        VolumeIdSearch = createSearchBuilder();
        VolumeIdSearch.and("volumeId", VolumeIdSearch.entity().getVolumeId(), SearchCriteria.Op.EQ);
        VolumeIdSearch.done();
        
        VolumeIdTypeSearch = createSearchBuilder();
        VolumeIdTypeSearch.and("volumeId", VolumeIdTypeSearch.entity().getVolumeId(), SearchCriteria.Op.EQ);
        VolumeIdTypeSearch.and("type", VolumeIdTypeSearch.entity().getsnapshotType(), SearchCriteria.Op.EQ);
        VolumeIdTypeSearch.done();
        
        VolumeIdVersionSearch = createSearchBuilder();
        VolumeIdVersionSearch.and("volumeId", VolumeIdTypeSearch.entity().getVolumeId(), SearchCriteria.Op.EQ);
        VolumeIdVersionSearch.and("version", VolumeIdTypeSearch.entity().getVersion(), SearchCriteria.Op.EQ);
        VolumeIdVersionSearch.done();
        
        ParentIdSearch = createSearchBuilder();
        ParentIdSearch.and("prevSnapshotId", ParentIdSearch.entity().getPrevSnapshotId(), SearchCriteria.Op.EQ);
        ParentIdSearch.done();
        
        backupUuidSearch = createSearchBuilder();
        backupUuidSearch.and("backupUuid", backupUuidSearch.entity().getBackupSnapshotId(), SearchCriteria.Op.EQ);
        backupUuidSearch.done();
       
    }
    
    @Override
    public long getLastSnapshot(long volumeId, long snapId) {
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        String sql = GET_LAST_SNAPSHOT;
        try {
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, volumeId);
            pstmt.setLong(2, snapId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception ex) {
            s_logger.error("error getting last snapshot", ex);
        }
        return 0;
    }

    @Override
    public long updateSnapshotVersion(long volumeId, String from, String to) {
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        String sql = UPDATE_SNAPSHOT_VERSION;
        try {
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setString(1, to);
            pstmt.setLong(2, volumeId);
            pstmt.setString(3, from);
            pstmt.executeUpdate();
            return 1;
        } catch (Exception ex) {
            s_logger.error("error getting last snapshot", ex);
        }
        return 0;
    }
}
