package net.shortninja.staffplus.staff.reporting.database;

import net.shortninja.staffplus.StaffPlus;
import net.shortninja.staffplus.common.Constants;
import net.shortninja.staffplus.event.ReportStatus;
import net.shortninja.staffplus.player.PlayerManager;
import net.shortninja.staffplus.player.SppPlayer;
import net.shortninja.staffplus.server.data.config.Options;
import net.shortninja.staffplus.staff.reporting.Report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public abstract class AbstractSqlReportRepository implements ReportRepository {

    private final PlayerManager playerManager;
    protected final Options options;
    private final String serverNameFilter;

    protected AbstractSqlReportRepository(PlayerManager playerManager, Options options) {
        this.playerManager = playerManager;
        this.options = options;
        serverNameFilter = !options.serverSyncConfiguration.isReportSyncEnabled() ? "AND (server_name is null OR server_name='" + options.serverName + "')" : "";
    }

    protected abstract Connection getConnection() throws SQLException;

    @Override
    public List<Report> getReports(UUID uuid, int offset, int amount) {
        List<Report> reports = new ArrayList<>();
        try (Connection sql = getConnection();
             PreparedStatement ps = sql.prepareStatement("SELECT * FROM sp_reports WHERE Player_UUID = ? AND deleted=? " + serverNameFilter + " ORDER BY timestamp DESC LIMIT ?,?")) {
            ps.setString(1, uuid.toString());
            ps.setBoolean(2, false);
            ps.setInt(3, offset);
            ps.setInt(4, amount);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reports.add(buildReport(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return reports;
    }

    @Override
    public List<Report> getReportsByOffender(UUID uuid) {
        List<Report> reports = new ArrayList<>();
        try (Connection sql = getConnection();
             PreparedStatement ps = sql.prepareStatement("SELECT * FROM sp_reports WHERE Player_UUID = ? AND deleted=? " + serverNameFilter + " ORDER BY timestamp DESC")) {
            ps.setString(1, uuid.toString());
            ps.setBoolean(2, false);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reports.add(buildReport(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return reports;
    }

    @Override
    public List<Report> getUnresolvedReports(int offset, int amount) {
        List<Report> reports = new ArrayList<>();
        try (Connection sql = getConnection();
             PreparedStatement ps = sql.prepareStatement("SELECT * FROM sp_reports WHERE status = ? AND deleted=? " + serverNameFilter + " ORDER BY timestamp DESC LIMIT ?,?")) {
            ps.setString(1, ReportStatus.OPEN.toString());
            ps.setBoolean(2, false);
            ps.setInt(3, offset);
            ps.setInt(4, amount);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reports.add(buildReport(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return reports;
    }

    @Override
    public List<Report> getClosedReports(int offset, int amount) {
        List<Report> reports = new ArrayList<>();
        try (Connection sql = getConnection();
             PreparedStatement ps = sql.prepareStatement("SELECT * FROM sp_reports WHERE status IN (?,?,?) AND deleted=? " + serverNameFilter + " ORDER BY timestamp DESC LIMIT ?,?")) {
            ps.setString(1, ReportStatus.REJECTED.toString());
            ps.setString(2, ReportStatus.RESOLVED.toString());
            ps.setString(3, ReportStatus.EXPIRED.toString());
            ps.setBoolean(4, false);
            ps.setInt(5, offset);
            ps.setInt(6, amount);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reports.add(buildReport(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return reports;
    }

    @Override
    public Optional<Report> findOpenReport(int reportId) {
        try (Connection sql = getConnection();
             PreparedStatement ps = sql.prepareStatement("SELECT * FROM sp_reports WHERE id = ? AND status = ? AND deleted=?")) {
            ps.setInt(1, reportId);
            ps.setString(2, ReportStatus.OPEN.toString());
            ps.setBoolean(3, false);
            try (ResultSet rs = ps.executeQuery()) {
                boolean first = rs.next();
                if (first) {
                    return Optional.of(buildReport(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Report> findReport(int reportId) {
        try (Connection sql = getConnection();
             PreparedStatement ps = sql.prepareStatement("SELECT * FROM sp_reports WHERE id = ? AND deleted=?")) {
            ps.setInt(1, reportId);
            ps.setBoolean(2, false);
            try (ResultSet rs = ps.executeQuery()) {
                boolean first = rs.next();
                if (first) {
                    return Optional.of(buildReport(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public void updateReport(Report report) {
        try (Connection sql = getConnection();
             PreparedStatement insert = sql.prepareStatement("UPDATE sp_reports set staff_name=?, staff_uuid=?, status=?, close_reason=? WHERE id=? AND deleted=?")) {
            insert.setString(1, report.getStaffName());
            insert.setString(2, report.getStaffUuid() != null ? report.getStaffUuid().toString() : null);
            insert.setString(3, report.getReportStatus().toString());
            insert.setString(4, report.getCloseReason());
            insert.setInt(5, report.getId());
            insert.setBoolean(6, false);
            insert.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void markReportDeleted(Report report) {
        try (Connection sql = getConnection();
             PreparedStatement insert = sql.prepareStatement("UPDATE sp_reports set deleted=? WHERE id=?")) {
            insert.setBoolean(1, true);
            insert.setInt(2, report.getId());
            insert.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Report> getAssignedReports(UUID staffUuid, int offset, int amount) {
        List<Report> reports = new ArrayList<>();
        try (Connection sql = getConnection();
             PreparedStatement ps = sql.prepareStatement("SELECT * FROM sp_reports WHERE staff_uuid = ? AND status = ? AND deleted=? " + serverNameFilter + " ORDER BY timestamp DESC LIMIT ?,?")) {
            ps.setString(1, staffUuid.toString());
            ps.setString(2, ReportStatus.IN_PROGRESS.toString());
            ps.setBoolean(3, false);
            ps.setInt(4, offset);
            ps.setInt(5, amount);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reports.add(buildReport(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return reports;
    }

    @Override
    public List<Report> getMyReports(UUID reporterUuid, int offset, int amount) {
        List<Report> reports = new ArrayList<>();
        try (Connection sql = getConnection();
             PreparedStatement ps = sql.prepareStatement("SELECT * FROM sp_reports WHERE Reporter_UUID = ? AND deleted=? " + serverNameFilter + " ORDER BY timestamp DESC LIMIT ?,?")) {
            ps.setString(1, reporterUuid.toString());
            ps.setBoolean(2, false);
            ps.setInt(3, offset);
            ps.setInt(4, amount);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reports.add(buildReport(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return reports;
    }

    @Override
    public List<Report> getMyReports(UUID reporterUuid) {
        List<Report> reports = new ArrayList<>();
        try (Connection sql = getConnection();
             PreparedStatement ps = sql.prepareStatement("SELECT * FROM sp_reports WHERE Reporter_UUID = ? AND deleted=? " + serverNameFilter + " ORDER BY timestamp")) {
            ps.setString(1, reporterUuid.toString());
            ps.setBoolean(2, false);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reports.add(buildReport(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return reports;
    }

    @Override
    public void removeReports(UUID playerUuid) {
        try (Connection sql = getConnection();
             PreparedStatement insert = sql.prepareStatement("DELETE FROM sp_reports WHERE Player_UUID = ? AND deleted=? " + serverNameFilter);) {
            insert.setString(1, playerUuid.toString());
            insert.setBoolean(2, false);
            insert.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<UUID, Integer> getReportedCount() {
        Map<UUID, Integer> count = new HashMap<>();
        try (Connection sql = getConnection();
             PreparedStatement ps = sql.prepareStatement("SELECT Player_UUID, count(*) as count FROM sp_reports WHERE Player_UUID is not null " + Constants.getServerNameFilterWithAnd(options.serverSyncConfiguration.isKickSyncEnabled()) + " GROUP BY Player_UUID ORDER BY count DESC")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    count.put(UUID.fromString(rs.getString("Player_UUID")), rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return count;
    }

    private Report buildReport(ResultSet rs) throws SQLException {
        String player_uuid = rs.getString("Player_UUID");
        UUID reporterUUID = UUID.fromString(rs.getString("Reporter_UUID"));
        UUID staffUUID = rs.getString("staff_uuid") != null ? UUID.fromString(rs.getString("staff_uuid")) : null;

        String reporterName;
        if (reporterUUID.equals(StaffPlus.get().consoleUUID)) {
            reporterName = "Console";
        } else {
            Optional<SppPlayer> reporter = playerManager.getOnOrOfflinePlayer(reporterUUID);
            reporterName = reporter.map(SppPlayer::getUsername).orElse(null);
        }

        UUID playerUUID = null;
        String culpritName = null;
        if (player_uuid != null) {
            playerUUID = UUID.fromString(player_uuid);
            Optional<SppPlayer> player = playerManager.getOnOrOfflinePlayer(playerUUID);
            culpritName = player.map(SppPlayer::getUsername).orElse(null);
        }

        int id = rs.getInt("ID");
        return new Report(playerUUID, culpritName, id,
            rs.getString("Reason"),
            reporterName,
            reporterUUID,
            rs.getLong("timestamp"),
            ReportStatus.valueOf(rs.getString("status")),
            rs.getString("staff_name"),
            staffUUID,
            rs.getString("close_reason"));
    }

}
