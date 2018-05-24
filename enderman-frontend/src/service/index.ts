import { apiUrl, easyFetch } from '../utils'

export interface IBranchInfo {
  name: string;
  hash: string;
}

export interface IBranches {
  branches: IBranchInfo[];
  totalLength: number;
  offset: number;
}

export interface ICommitFileRequest {
  path: string;
  offset: number;
  length: number;
}

export interface ICommitDiffPathsRequest {
  hash1: string;
  hash2: string;
  depth: number;
  prefix: string;
}

export interface ICommitFileInfo {
  path: string;
  content: string[];
  totalLine: number;
  offsetLine: number;
}

export interface IFileTreeItem {
  itemType: 'file' | 'dir';
  name: string;
  fullPath: string;
  children: IFileTreeItem[];
  skipped: boolean
}

export enum DiffType {
  Same = 0,
  Remove1,
  Insert2,
  Remove1Insert2,
}

export interface ICommitDiffFileRequest {
  hash1: string,
  hash2: string,
  path: string,
  offset: number,
  length: number,
}

export interface InnerLine {
  lineNumberInFile: number;
  content: string;
}

export interface ICommitDiffFileLine {
  diffType: DiffType;
  chunks: InnerLine[];
}

export interface ICommitDiffFileContent {
  displayLineOffset: number;
  totalDisplayLine: number;
  file1TotalLine: number;
  file2TotalLIne: number;
  lines: ICommitDiffFileLine[];
}

export const Services = {

  fetchBranchInfo(offset: number = 0, length: number = 100) {
    return easyFetch<IBranches>(`${apiUrl}/branch?offset=${offset}&length=${length}`);
  },

  fetchCommitFileContent(hash: string, req: ICommitFileRequest) {
    return easyFetch<ICommitFileInfo>(`${apiUrl}/commit/${hash}/file`, {
      method: 'POST',
      body: JSON.stringify(req),
      headers: {
        'Content-Type': 'application/json',
      },
    });
  },

  fetchCommitPathInfo(hash: string, depth: number = 3, prefix: string = "/") {
    return easyFetch<IFileTreeItem>(`${apiUrl}/commit/${hash}/file?depth=${depth}&prefix=${prefix}`);
  },

  fetchCommitDiffPaths(hash1: string, hash2: string, depth: number = 3, prefix: string = "/") {
    const data: ICommitDiffPathsRequest = {
      hash1,
      hash2,
      depth,
      prefix
    };
    return easyFetch<IFileTreeItem>(`${apiUrl}/commit/diff/paths`, {
      method: 'POST',
      body: JSON.stringify(data),
      headers: {
        'Content-Type': 'application/json',
      },
    });
  },

  fetchCommitDiffFileContent(
    hash1: string,
    hash2: string,
    path: string,
    offset: number = 0,
    length: number = 200,
  ) {
    const data: ICommitDiffFileRequest = {
      hash1,
      hash2,
      path,
      offset,
      length,
    };
    return easyFetch<ICommitDiffFileContent>(`${apiUrl}/commit/diff/content`, {
      method: 'POST',
      body: JSON.stringify(data),
      headers: {
        'Content-Type': 'application/json',
      },
    });
  },

}
