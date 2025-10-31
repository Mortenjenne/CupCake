package app.services;

import app.exceptions.DatabaseException;

import java.time.YearMonth;

public interface StatisticService
{
    public double getTotalRevenue(int adminId) throws DatabaseException;

    public double getMonthlyRevenue(int adminId, YearMonth month) throws DatabaseException;

    public double getAverageOrderValue(int adminId) throws DatabaseException;
}
