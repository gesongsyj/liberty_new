/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50726
Source Host           : 127.0.0.1:3306
Source Database       : liberty

Target Server Type    : MYSQL
Target Server Version : 50726
File Encoding         : 65001

Date: 2021-06-30 17:48:36
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for account
-- ----------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `accountName` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `accountType` char(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for centre
-- ----------------------------
DROP TABLE IF EXISTS `centre`;
CREATE TABLE `centre` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `startDate` datetime DEFAULT NULL,
  `endDate` datetime DEFAULT NULL,
  `centreMax` double(20,6) DEFAULT NULL,
  `centreMin` double(20,6) DEFAULT NULL,
  `max` double(20,6) DEFAULT NULL,
  `min` double(20,6) DEFAULT NULL,
  `currencyId` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for currency
-- ----------------------------
DROP TABLE IF EXISTS `currency`;
CREATE TABLE `currency` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(10) NOT NULL,
  `name` varchar(50) NOT NULL,
  `followed` tinyint(1) NOT NULL DEFAULT '0',
  `currencyType` varchar(10) NOT NULL,
  `lastSeoDate` datetime DEFAULT NULL COMMENT '最近除权日',
  `totalStockCount` double(255,0) DEFAULT NULL,
  `followedDate` datetime DEFAULT NULL,
  `calibrated` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否验证过策略,跑全量验证时避免重复跑,1:是;0:否',
  PRIMARY KEY (`id`),
  KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=14057 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for currency_strategy
-- ----------------------------
DROP TABLE IF EXISTS `currency_strategy`;
CREATE TABLE `currency_strategy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `currencyId` bigint(20) DEFAULT NULL,
  `strategyId` bigint(20) DEFAULT NULL,
  `startDate` datetime DEFAULT NULL,
  `cutLine` double(20,6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `cId_sId_date` (`currencyId`,`strategyId`,`startDate`)
) ENGINE=InnoDB AUTO_INCREMENT=1068940 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for dictionary
-- ----------------------------
DROP TABLE IF EXISTS `dictionary`;
CREATE TABLE `dictionary` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `key` varchar(10) DEFAULT NULL,
  `value` varchar(50) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  `description` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for kline
-- ----------------------------
DROP TABLE IF EXISTS `kline`;
CREATE TABLE `kline` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` datetime DEFAULT NULL,
  `max` double(255,6) DEFAULT NULL,
  `min` double(255,6) DEFAULT NULL,
  `open` double(255,6) DEFAULT NULL,
  `close` double(255,6) DEFAULT NULL,
  `currencyId` bigint(20) DEFAULT NULL,
  `strokeId` bigint(20) DEFAULT NULL,
  `type` char(1) DEFAULT NULL,
  `bar` double(255,6) DEFAULT NULL,
  `ema_s` double(255,6) DEFAULT NULL,
  `ema_l` double(255,6) DEFAULT NULL,
  `diff` double(255,6) DEFAULT NULL,
  `dea` double(255,6) DEFAULT NULL,
  `aoi` double(255,6) DEFAULT NULL COMMENT '涨幅',
  `volume` double(255,6) DEFAULT NULL COMMENT ' 成交量',
  `turnover` double(255,6) DEFAULT NULL COMMENT '成交额',
  `turnoverRate` double(255,6) DEFAULT NULL COMMENT '换手率',
  `bosp` varchar(255) DEFAULT NULL COMMENT '买卖点;0:买,1:卖',
  PRIMARY KEY (`id`),
  KEY `currencyId` (`currencyId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=142942117 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for line
-- ----------------------------
DROP TABLE IF EXISTS `line`;
CREATE TABLE `line` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `max` double(255,6) DEFAULT NULL,
  `min` double(255,6) DEFAULT NULL,
  `startDate` datetime DEFAULT NULL,
  `endDate` datetime DEFAULT NULL,
  `parentId` bigint(20) DEFAULT NULL,
  `currencyId` bigint(20) DEFAULT NULL,
  `prevId` bigint(20) DEFAULT NULL,
  `nextId` bigint(20) DEFAULT NULL,
  `type` char(1) DEFAULT NULL,
  `direction` char(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `currencyId` (`currencyId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for shape
-- ----------------------------
DROP TABLE IF EXISTS `shape`;
CREATE TABLE `shape` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` char(1) DEFAULT NULL,
  `max` double(255,6) DEFAULT NULL,
  `min` double(255,6) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for strategy
-- ----------------------------
DROP TABLE IF EXISTS `strategy`;
CREATE TABLE `strategy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `describe` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for stroke
-- ----------------------------
DROP TABLE IF EXISTS `stroke`;
CREATE TABLE `stroke` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `max` double(255,6) DEFAULT NULL,
  `min` double(255,6) DEFAULT NULL,
  `startDate` datetime DEFAULT NULL,
  `endDate` datetime DEFAULT NULL,
  `currencyId` bigint(20) DEFAULT NULL,
  `lineId` bigint(20) DEFAULT NULL,
  `prevId` bigint(20) DEFAULT NULL,
  `nextId` bigint(20) DEFAULT NULL,
  `type` char(1) DEFAULT NULL,
  `direction` char(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `currencyId` (`currencyId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=48093061 DEFAULT CHARSET=utf8mb4;
