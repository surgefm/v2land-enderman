import {observable, computed} from 'mobx';
import { Services, IBranches, IBranchInfo, IFileTreeItem } from "../service";

import { sleep } from "../utils";

export enum IAppMode {
  Normal,
  Diff,
}

export class AppState {
  
}